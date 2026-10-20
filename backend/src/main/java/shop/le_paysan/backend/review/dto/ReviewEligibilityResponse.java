package shop.le_paysan.backend.review.dto;

public record ReviewEligibilityResponse(boolean eligible, String reason) {

    public static final String REASON_OWNER = "OWNER";
    public static final String REASON_NOT_PURCHASED = "NOT_PURCHASED";

    public static ReviewEligibilityResponse ofEligible() {
        return new ReviewEligibilityResponse(true, null);
    }

    public static ReviewEligibilityResponse ofIneligible(String reason) {
        return new ReviewEligibilityResponse(false, reason);
    }
}
