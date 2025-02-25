package app.forku.domain.model.checklist

import app.forku.domain.model.checklist.Answer

data class CheckedItem(
    val id: String,
    val expectedAnswer: Answer,
    val userAnswer: Answer
) 