package com.cleanguard.ai.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class DeleteDownloadUseCase @Inject constructor() {

    suspend operator fun invoke(filePath: String): Boolean = withContext(Dispatchers.IO) {
        File(filePath).delete()
    }
}
