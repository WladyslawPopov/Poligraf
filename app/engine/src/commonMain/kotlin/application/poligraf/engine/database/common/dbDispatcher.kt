package application.poligraf.engine.database.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

val dbDispatcher: CoroutineDispatcher = Dispatchers.IO
