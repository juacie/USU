package com.unitech.scanner.utility.config;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/1/28 下午 05:21
 * 修改人:user
 * 修改時間:2021/1/28 下午 05:21
 * 修改備註:
 */

public enum BarcodeType {
    CODE_39(1),
    CODABAR(2),
    CODE_128(3),
    KIX(4),
    KOREAN_POSTAL(5),
    INTERLEAVED_2of5(6),
    CODE_93(7),
    UPC_A(8),
    UPC_E(9),
    EAN_8(10),
    EAN_13(11),
    CODE_11(12),
    UPC_E_COMPOSITE(14),
    GS1_128(15),
    PDF417(17),
    ISBN_EAN(22),
    MICRO_PDF417(26),
    DATA_MATRIX(27),
    QR_CODE(28),
    USPS_POST_NET(30),
    PLANET_CODE_12(31),
    JAPAN_POSTAL(34),
    AUSTRALIA_POST(35),
    ROYAL_MAIL_4_STATE(39),
    MICRO_QR_CODE(44),
    AZTEC(45),
    GS1_DATA_BAR(48),
    GS1_DATA_BAR_LIMITED(49),
    GS1_DATA_BAR_EXPANDED(50),
    ISSN(54),
    GS1_DATA_BAR_EXPANDED_COMPOSITE(84),
    GS1_DATA_BAR_LIMITED_COMPOSITE(85),
    GS1_DATA_BAR_COMPOSITE(86),
    EAN_8_COMPOSITE(153),
    HAN_XIN(183)
    ;
    private final int type;

    BarcodeType(int type) {
        this.type = type;
    }

    public int get() {
        return type;
    }
    public static BarcodeType fromValue(int value) throws IllegalArgumentException {
        for (BarcodeType e : BarcodeType.values()) {
            if (e.type == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown enum value :" + value);
    }
}