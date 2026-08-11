import Foundation
import AVFoundation
import Combine
import UIKit
import SharedLogic

/**
 * A high-performance, thread-safe native iOS audio engine.
 * Solves main-thread blocking and state synchronization issues once and for all.
 */
class NativeVoiceRecorderEngine: NSObject, ObservableObject, AVAudioPlayerDelegate, IosAudioRecorderDelegate {
    
    // MARK: - Published State (For SwiftUI)
    @Published var isRecording = false
    @Published var isPlaying = false
    @Published var currentTime: TimeInterval = 0
    @Published var duration: TimeInterval = 0
    @Published var amplitudes: [Float] = []
    @Published var latestAmplitude: Float = 0
    
    // MARK: - Internal State (Thread Safe Storage)
    private let internalQueue = DispatchQueue(label: "application.liedetector.recorder.engine", qos: .userInitiated)
    private weak var recorderBridge: IosAudioRecorder?
    
    private var recorder: AVAudioRecorder?
    private var player: AVAudioPlayer?
    private var displayLink: CADisplayLink?
    
    private var internalFileURL: URL?
    private var internalAmplitudes: [Float] = []
    private var internalDuration: TimeInterval = 0
    private var internalCurrentTime: TimeInterval = 0
    private var internalIsRecording = false
    private var internalIsPlaying = false

    private let amplitudeSampleRate: TimeInterval = 0.033
    private var lastAmplitudeSampleTime: TimeInterval = 0
    private var lastBridgeUpdate: TimeInterval = 0
    private let bridgeUpdateThrottle: TimeInterval = 0.1

    private let impactFeedback = UIImpactFeedbackGenerator(style: .light)
    
    // MARK: - Init
    
    init(bridge: IosAudioRecorder? = nil) {
        self.recorderBridge = bridge
        super.init()
        setupAudioSession()
        impactFeedback.prepare()
        bridge?.setDelegate(delegate: self)
    }
    
    private func setupAudioSession() {
        let session = AVAudioSession.sharedInstance()
        try? session.setCategory(.playAndRecord, mode: .default, options: [.defaultToSpeaker, .allowBluetoothHFP])
        try? session.setActive(true)
    }

    // MARK: - SwiftUI Helpers
    
    func startRecording() { start() }
    func pauseRecording() { pause() }
    func stopRecording(completion: @escaping (URL, Int64, [Float]) -> Void) {
        Task {
            do {
                _ = try await self.__stop()
                let url = self.internalFileURL
                let dur = self.internalDuration
                let amps = self.internalAmplitudes
                
                DispatchQueue.main.async {
                    if let u = url { completion(u, Int64(dur * 1000), amps) }
                }
            } catch {
                print("Stop recording failed: \(error)")
            }
        }
    }
    func seek(to time: TimeInterval) { seekTo(positionMillis: Int64(time * 1000)) }
    func togglePlayPause() { internalQueue.async { if self.internalIsPlaying { self.pausePlayback() } else { self.play() } } }
    func skip(by seconds: TimeInterval) { 
        internalQueue.async {
            let target = (self.internalCurrentTime + seconds)
            self.seekTo(positionMillis: Int64(target * 1000)) 
        }
    }

    // MARK: - Display Link
    
    private func startDisplayLink() {
        DispatchQueue.main.async {
            self.displayLink?.invalidate()
            self.displayLink = CADisplayLink(target: self, selector: #selector(self.updateLoop))
            self.displayLink?.preferredFramesPerSecond = 60
            self.displayLink?.add(to: .main, forMode: .common)
        }
    }
    
    private func stopDisplayLink() {
        DispatchQueue.main.async {
            self.displayLink?.invalidate()
            self.displayLink = nil
        }
    }
    
    @objc private func updateLoop() {
        internalQueue.async {
            let now = CACurrentMediaTime()
            
            if let recorder = self.recorder, recorder.isRecording {
                recorder.updateMeters()
                let power = recorder.averagePower(forChannel: 0)
                let amp = pow(10, Double(power) / 20)
                let latest = Float(amp).clampedTo01()

                let elapsed = now - self.lastAmplitudeSampleTime
                
                if elapsed >= self.amplitudeSampleRate {
                    let samplesToAppend = Int(elapsed / self.amplitudeSampleRate)
                    let normalized = sqrt(latest).clampedTo01()
                    for _ in 0..<samplesToAppend {
                        self.internalAmplitudes.append(normalized)
                    }
                    self.lastAmplitudeSampleTime += Double(samplesToAppend) * self.amplitudeSampleRate

                    let visualDuration = Double(self.internalAmplitudes.count) * self.amplitudeSampleRate
                    self.internalDuration = visualDuration
                    self.internalCurrentTime = visualDuration
                    
                    if now - self.lastBridgeUpdate >= self.bridgeUpdateThrottle {
                        self.recorderBridge?.addAmplitude(amplitude: normalized)
                        self.recorderBridge?.updateDuration(millis: Int64(visualDuration * 1000))
                        self.lastBridgeUpdate = now
                    }

                    self.syncPublishedState(latestAmp: latest)
                }
            } else if let player = self.player, player.isPlaying {
                self.internalCurrentTime = player.currentTime
                
                if now - self.lastBridgeUpdate >= self.bridgeUpdateThrottle {
                    self.recorderBridge?.updatePlayback(playing: true, positionMillis: Int64(player.currentTime * 1000))
                    self.lastBridgeUpdate = now
                }
                
                self.syncPublishedState()
            }
        }
    }

    private func syncPublishedState(latestAmp: Float = 0) {
        let cur = internalCurrentTime
        let dur = internalDuration
        let amps = internalAmplitudes
        let recording = internalIsRecording
        let playing = internalIsPlaying

        DispatchQueue.main.async {
            self.currentTime = cur
            self.duration = dur
            self.amplitudes = amps
            self.latestAmplitude = latestAmp
            self.isRecording = recording
            self.isPlaying = playing
        }
    }

    func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        internalQueue.async {
            self.internalIsPlaying = false
            self.internalCurrentTime = self.internalDuration
            self.stopDisplayLink()
            self.recorderBridge?.updatePlayback(playing: false, positionMillis: Int64(self.internalDuration * 1000))
            self.syncPublishedState()
        }
    }
    
    private func resetInternalState() {
        player?.stop()
        player = nil
        recorder?.stop()
        recorder = nil
        internalFileURL = nil
        internalDuration = 0
        internalCurrentTime = 0
        internalAmplitudes = []
        internalIsRecording = false
        internalIsPlaying = false
        
        recorderBridge?.updateRecordingState(recording: false, paused: false)
        recorderBridge?.updateDuration(millis: 0)
        recorderBridge?.setAmplitudes(amplitudes: [])

        syncPublishedState()
    }

    // MARK: - IosAudioRecorderDelegate Implementation
    
    func start() {
        internalQueue.async {
            if self.internalIsRecording { return }
            if self.internalFileURL == nil { self.resetInternalState() }

            self.player?.stop()

            let tempDir = NSTemporaryDirectory()
            let partURL = URL(fileURLWithPath: tempDir).appendingPathComponent("part_\(UUID().uuidString).m4a")
            let settings: [String: Any] = [
                AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
                AVSampleRateKey: 44100.0,
                AVNumberOfChannelsKey: 1,
                AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue
            ]
            
            do {
                self.recorder = try AVAudioRecorder(url: partURL, settings: settings)
                self.recorder?.isMeteringEnabled = true
                self.recorder?.prepareToRecord()
                self.recorder?.record()

                self.internalIsRecording = true
                self.lastAmplitudeSampleTime = CACurrentMediaTime()
                self.recorderBridge?.updateRecordingState(recording: true, paused: false)
                self.startDisplayLink()
                self.syncPublishedState()
            } catch { print("NativeEngine: Start failed \(error)") }
        }
    }
    
    func pause() {
        internalQueue.async {
            self.recorder?.pause()
            self.internalIsRecording = false
            self.stopDisplayLink()
            self.recorderBridge?.updateRecordingState(recording: true, paused: true)
            self.syncPublishedState()
        }
    }
    
    func resume() {
        internalQueue.async {
            self.recorder?.record()
            self.internalIsRecording = true
            self.lastAmplitudeSampleTime = CACurrentMediaTime()
            self.recorderBridge?.updateRecordingState(recording: true, paused: false)
            self.startDisplayLink()
            self.syncPublishedState()
        }
    }
    
    func __stop() async throws -> String? {
        return await withCheckedContinuation { continuation in
            internalQueue.async {
                guard let recorder = self.recorder else {
                    continuation.resume(returning: self.internalFileURL?.path)
                    return
                }

                let partURL = recorder.url
                recorder.stop()
                self.recorder = nil
                self.internalIsRecording = false
                self.stopDisplayLink()
                self.recorderBridge?.updateRecordingState(recording: false, paused: false)

                if let existing = self.internalFileURL, existing != partURL {
                    Task {
                        if let merged = await AudioProcessor.merge(url1: existing, url2: partURL) {
                            self.internalQueue.async {
                                self.internalFileURL = merged
                                self.internalDuration = (try? AVAudioPlayer(contentsOf: merged).duration) ?? self.internalDuration
                                self.internalCurrentTime = self.internalDuration
                                self.recorderBridge?.updateDuration(millis: Int64(self.internalDuration * 1000))
                                self.syncPublishedState()
                                continuation.resume(returning: merged.path)
                            }
                        } else {
                            continuation.resume(returning: nil)
                        }
                    }
                } else {
                    self.internalFileURL = partURL
                    self.internalCurrentTime = self.internalDuration
                    self.syncPublishedState()
                    continuation.resume(returning: partURL.path)
                }
            }
        }
    }
    
    func cancel() {
        internalQueue.async {
            self.recorder?.stop()
            self.recorder?.deleteRecording()
            self.resetInternalState()
        }
    }

    func play() {
        internalQueue.async {
            guard let url = self.internalFileURL else { return }

            if self.player == nil || self.player?.url != url {
                self.player = try? AVAudioPlayer(contentsOf: url)
                self.player?.delegate = self
                self.player?.prepareToPlay()
            }

            if let p = self.player {
                if p.currentTime >= p.duration - 0.05 { p.currentTime = 0 }
                p.play()
                self.internalIsPlaying = true
                self.startDisplayLink()
                self.recorderBridge?.updatePlayback(playing: true, positionMillis: Int64(p.currentTime * 1000))
            }
            self.syncPublishedState()
        }
    }
    
    func pausePlayback() {
        internalQueue.async {
            self.player?.pause()
            self.internalIsPlaying = false
            self.stopDisplayLink()
            self.recorderBridge?.updatePlayback(playing: false, positionMillis: Int64(self.internalCurrentTime * 1000))
            self.syncPublishedState()
        }
    }
    
    func seekTo(positionMillis: Int64) {
        internalQueue.async {
            let time = TimeInterval(positionMillis) / 1000.0
            if let p = self.player {
                p.currentTime = max(0, min(time, p.duration))
            }
            self.internalCurrentTime = max(0, min(time, self.internalDuration))
            self.syncPublishedState()
        }
    }

    func __trim(startMillis: Int64, endMillis: Int64) async throws -> String? {
        return await withCheckedContinuation { continuation in
            internalQueue.async {
                guard let url = self.internalFileURL else { continuation.resume(returning: nil); return }
                Task {
                    if let trimmedURL = await AudioProcessor.trim(url: url, startMs: startMillis, endMs: endMillis) {
                        self.internalQueue.async {
                            self.internalFileURL = trimmedURL
                            self.internalDuration = Double(endMillis - startMillis) / 1000.0
                            self.internalCurrentTime = 0

                            let startIdx = Int(Double(startMillis) / 33.0)
                            let endIdx = Int(Double(endMillis) / 33.0)
                            if startIdx < self.internalAmplitudes.count {
                                self.internalAmplitudes = Array(self.internalAmplitudes[startIdx..<min(endIdx, self.internalAmplitudes.count)])
                                self.recorderBridge?.setAmplitudes(amplitudes: self.internalAmplitudes.map { KotlinFloat(value: $0) })
                            }

                            self.recorderBridge?.updateDuration(millis: Int64(self.internalDuration * 1000))
                            self.player = nil
                            self.syncPublishedState()
                            continuation.resume(returning: trimmedURL.path)
                        }
                    } else {
                        continuation.resume(returning: nil)
                    }
                }
            }
        }
    }
    
    func replace(positionMillis: Int64) {
        Task {
            if positionMillis < Int64(self.internalDuration * 1000) && positionMillis > 0 {
                _ = try? await self.__trim(startMillis: 0, endMillis: positionMillis)
            } else if positionMillis == 0 {
                self.internalQueue.async {
                    self.resetInternalState()
                }
            }
            self.start()
        }
    }
    
    func __loadFile(path: String, amplitudes: [KotlinFloat]?) async throws {
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            internalQueue.async {
                self.resetInternalState()
                let url = path.contains("/") ? URL(fileURLWithPath: path) : 
                    FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0].appendingPathComponent(path)
                
                if !FileManager.default.fileExists(atPath: url.path) {
                    continuation.resume()
                    return
                }
                
                self.internalFileURL = url
                let asset = AVURLAsset(url: url)
                
                Task {
                    do {
                        let dur = try await asset.load(.duration).seconds
                        self.internalQueue.async {
                            self.internalDuration = dur
                            self.internalCurrentTime = 0
                            if let amps = amplitudes {
                                self.internalAmplitudes = amps.map { $0.floatValue }
                                self.recorderBridge?.setAmplitudes(amplitudes: amps)
                            }
                            self.recorderBridge?.updateDuration(millis: Int64(dur * 1000))
                            self.syncPublishedState()
                            continuation.resume()
                        }
                    } catch {
                        continuation.resume(throwing: error)
                    }
                }
            }
        }
    }

    // SwiftUI Helper (Non-async wrapper)
    func loadFile(path: String, amplitudes: [KotlinFloat]?) {
        Task {
            try? await __loadFile(path: path, amplitudes: amplitudes)
        }
    }
}

// MARK: - Heavy Audio Processing (Swift 6 Compliant)
struct AudioProcessor {
    static func merge(url1: URL, url2: URL) async -> URL? {
        let composition = AVMutableComposition()
        guard let track = composition.addMutableTrack(withMediaType: .audio, preferredTrackID: kCMPersistentTrackID_Invalid) else { return nil }
        do {
            let asset1 = AVURLAsset(url: url1); let asset2 = AVURLAsset(url: url2)
            let d1 = try await asset1.load(.duration); let d2 = try await asset2.load(.duration)
            guard let t1 = try await asset1.loadTracks(withMediaType: .audio).first,
                  let t2 = try await asset2.loadTracks(withMediaType: .audio).first else { return nil }
            try track.insertTimeRange(CMTimeRange(start: .zero, duration: d1), of: t1, at: .zero)
            try track.insertTimeRange(CMTimeRange(start: .zero, duration: d2), of: t2, at: d1)
            let out = URL(fileURLWithPath: NSTemporaryDirectory() + "merged_\(UUID().uuidString).m4a")
            guard let exp = AVAssetExportSession(asset: composition, presetName: AVAssetExportPresetAppleM4A) else { return nil }
            exp.outputURL = out; exp.outputFileType = .m4a
            await exp.export()
            return exp.status == .completed ? out : nil
        } catch { return nil }
    }
    
    static func trim(url: URL, startMs: Int64, endMs: Int64) async -> URL? {
        let asset = AVURLAsset(url: url)
        let timeRange = CMTimeRange(start: CMTime(value: startMs, timescale: 1000), 
                                    duration: CMTime(value: endMs - startMs, timescale: 1000))
        let out = URL(fileURLWithPath: NSTemporaryDirectory() + "trimmed_\(UUID().uuidString).m4a")
        guard let exp = AVAssetExportSession(asset: asset, presetName: AVAssetExportPresetAppleM4A) else { return nil }
        exp.outputURL = out; exp.outputFileType = .m4a; exp.timeRange = timeRange
        await exp.export()
        return exp.status == .completed ? out : nil
    }
}

extension Float {
    func clampedTo01() -> Float { Swift.min(Swift.max(self, 0), 1) }
}
