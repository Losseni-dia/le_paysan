package shop.le_paysan.backend.shop.dto;

import shop.le_paysan.backend.shop.ShopPayoutAccount;

public record PayoutAccountResponse(String operator, String phoneNumber, String accountHolderName, boolean verified) {

    public static PayoutAccountResponse from(ShopPayoutAccount a) {
        return new PayoutAccountResponse(a.getOperator(), a.getPhoneNumber(), a.getAccountHolderName(), a.isVerified());
    }
}
