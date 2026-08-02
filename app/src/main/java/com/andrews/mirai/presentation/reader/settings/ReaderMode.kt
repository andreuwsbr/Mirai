package com.andrews.mirai.presentation.reader.settings

enum class ReaderMode(val label: String) {
    LONG_STRIP("Tira longa"),
    LONG_STRIP_GAPS("Tira longa com lacunas"),
    PAGED_LEFT_TO_RIGHT("Paginado (esquerda para direita)"),
    PAGED_RIGHT_TO_LEFT("Paginado (direita para esquerda)"),
    PAGED_VERTICAL("Paginado (vertical)")
}

enum class ReaderBackground(val label: String) {
    BLACK("Preto"),
    GRAY("Cinza"),
    WHITE("Branco")
}
