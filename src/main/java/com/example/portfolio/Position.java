package com.example.portfolio;

public class Position {
    private final float originalPrice;
    private final int quantity;
    private final FinancialAsset asset;

    public Position(float originalPrice, int quantity, FinancialAsset asset) {
        try {
            if (asset == null) {
                Logger.log(LogLevel.ERROR, "Position constructor: asset is null");
                throw new IllegalArgumentException("asset must not be null");
            }
            if (!Float.isFinite(originalPrice)) {
                Logger.log(LogLevel.ERROR, "Position constructor: originalPrice is not finite for symbol=" + safeSymbol(asset) + " value=" + originalPrice);
                throw new IllegalArgumentException("originalPrice must be a finite number");
            }
            this.originalPrice = originalPrice;
            this.quantity = quantity;
            this.asset = asset;
        } catch (Exception e) {
            Logger.log(LogLevel.CRITICAL, "Failed to create Position. qty=" + quantity + " originalPrice=" + originalPrice);
            throw e;
        }
    }

    public float getOriginalPrice() {
        return originalPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public FinancialAsset getAsset() {
        return asset;
    }

    public float getCurrentAssetPrice() {
        try {
            float current = asset.getCurrentPrice();
            if (!Float.isFinite(current)) {
                Logger.log(LogLevel.ERROR, "Current asset price is not finite for symbol=" + getSymbol() + " price=" + current);
                throw new IllegalStateException("Current asset price is not finite for symbol: " + getSymbol());
            }
            return current;
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getCurrentAssetPrice failed for symbol=" + safeSymbol(asset));
            throw e;
        }
    }

    public String getSymbol() {
        try {
            return asset.getSymbol();
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getSymbol failed in Position");
            throw e;
        }
    }

    public boolean isLong() {
        return quantity > 0;
    }

    public boolean isShort() {
        return quantity < 0;
    }

    public float getTotalValue() {
        try {
            if (quantity == 0) return 0.0f;
            return quantity * getCurrentAssetPrice();
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getTotalValue failed for symbol=" + safeSymbol(asset) + " qty=" + quantity);
            throw e;
        }
    }

    public float getCostBasis() {
        try {
            if (quantity == 0) return 0.0f;
            return quantity * originalPrice;
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getCostBasis failed for symbol=" + safeSymbol(asset) + " qty=" + quantity);
            throw e;
        }
    }

    public float getProfit() {
        try {
            if (quantity == 0) return 0.0f;
            return quantity * (getCurrentAssetPrice() - originalPrice);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getProfit failed for symbol=" + safeSymbol(asset) + " qty=" + quantity);
            throw e;
        }
    }

    public float getProfitPercentage() {
        try {
            if (quantity == 0) return 0.0f;
            float denom = Math.abs(getCostBasis());
            if (!Float.isFinite(denom) || denom == 0.0f) return 0.0f;
            return (getProfit() / denom) * 100.0f;
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getProfitPercentage failed for symbol=" + safeSymbol(asset) + " qty=" + quantity);
            throw e;
        }
    }

    @Override
    public String toString() {
        try {
            return "Position{" +
                    "symbol='" + getSymbol() + '\'' +
                    ", quantity=" + quantity +
                    ", originalPrice=" + originalPrice +
                    '}';
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "Position.toString failed");
            return "Position{<toString failed>}";
        }
    }

    private static String safeSymbol(FinancialAsset a) {
        try {
            return a == null ? "null" : a.getSymbol();
        } catch (Exception e) {
            return "<symbol?>";
        }
    }
}