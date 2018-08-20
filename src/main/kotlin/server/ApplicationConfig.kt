package server

data class ApplicationConfig(
    var files: String = "",
    var port: Int = 8080,
    var host: String = "",
    var mappings: List<Mapping> = listOf()
)

data class Mapping(
    var prefix: String = "",
    var host: String = ""
)
