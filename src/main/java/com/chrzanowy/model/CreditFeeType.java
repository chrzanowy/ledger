package com.chrzanowy.model;

import static com.chrzanowy.model.CreditFeeGroup.CORRECTIONS;
import static com.chrzanowy.model.CreditFeeGroup.MARKETING;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CreditFeeType {
    OPERATOR_ERROR(CORRECTIONS),
    SYSTEM_ERROR(CORRECTIONS),
    REFERRAL_PROGRAM(MARKETING),
    RENT_DISCOUNTS(MARKETING),
    OTHER(MARKETING);

    private final CreditFeeGroup creditFeeGroup;
}
