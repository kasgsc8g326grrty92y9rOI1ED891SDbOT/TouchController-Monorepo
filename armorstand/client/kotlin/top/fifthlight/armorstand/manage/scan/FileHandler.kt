package top.fifthlight.armorstand.manage.scan

import top.fifthlight.blazerod.model.loader.LoadContext
import top.fifthlight.blazerod.model.loader.ModelFileLoader
import top.fifthlight.blazerod.model.loader.ThumbnailResult
import java.nio.file.Path

interface FileHandler {
    fun isFileToScan(file: Path): Boolean
    fun getLoaderOfMarkedFile(file: Path): ModelFileLoader?
    fun isModelFile(file: Path): Boolean
    fun isAnimationFile(file: Path): Boolean
    fun canExtractEmbedThumbnail(file: Path): Boolean
    fun extractEmbedThumbnail(file: Path, context: LoadContext): ThumbnailResult?
}
