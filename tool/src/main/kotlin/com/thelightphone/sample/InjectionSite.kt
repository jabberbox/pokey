package com.thelightphone.sample

enum class BodyPart(val label: String) {
    ABDOMEN("Abdomen"),
    THIGH("Thigh"),
    ARM("Arm"),
}

enum class InjectionSite(val label: String, val bodyPart: BodyPart, val shortLabel: String = label) {
    LEFT_ABDOMEN("Abdomen: Left", BodyPart.ABDOMEN, shortLabel = "Abd: Left"),
    RIGHT_ABDOMEN("Abdomen: Right", BodyPart.ABDOMEN, shortLabel = "Abd: Right"),
    LEFT_THIGH("Thigh: Left", BodyPart.THIGH),
    RIGHT_THIGH("Thigh: Right", BodyPart.THIGH),
    LEFT_ARM("Arm: Left", BodyPart.ARM),
    RIGHT_ARM("Arm: Right", BodyPart.ARM),
}
