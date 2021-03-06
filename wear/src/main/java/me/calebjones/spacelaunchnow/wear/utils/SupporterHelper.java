package me.calebjones.spacelaunchnow.wear.utils;


import io.realm.Realm;
import me.calebjones.spacelaunchnow.data.models.Products;

public class SupporterHelper {
    // SKU for our subscription (infinite gas)
    public static final String SKU_TWO_DOLLAR = "two_dollar_support";
    public static final String SKU_SIX_DOLLAR = "six_dollar_support";
    public static final String SKU_TWELVE_DOLLAR = "twelve_dollar_support";
    public static final String SKU_THIRTY_DOLLAR = "thirty_dollar_support";
    public static final String SKU_OTHER = "beta_supporter";

    public static Products getProduct(String productID){
        Products product = new Products();
        if (productID.equals(SKU_TWO_DOLLAR)) {
            product.setName("Founder 2016 - Bronze");
            product.setDescription("This ensures you will always have access to every supporter features.");
            product.setType("Supporter");
            product.setPrice(2);
        } else if (productID.equals(SKU_SIX_DOLLAR)){
            product.setName("Founder 2016 - Silver");
            product.setDescription("This ensures you will always have access to every supporter features.");
            product.setType("Supporter");
            product.setPrice(6);
        } else if (productID.equals(SKU_TWELVE_DOLLAR)){
            product.setName("Founder 2016 - Gold");
            product.setDescription("This ensures you will always have access to every supporter features.");
            product.setType("Supporter");
            product.setPrice(12);
        } else if (productID.equals(SKU_THIRTY_DOLLAR)) {
            product.setName("Founder 2016 - Platinum");
            product.setDescription("This ensures you will always have access to every supporter features.");
            product.setType("Supporter");
            product.setPrice(30);
        }
        return product;
    }

    public static boolean isSupporter(){
        Realm realm = Realm.getDefaultInstance();
        if (realm.where(Products.class).findFirst() != null){
            realm.close();
            return true;
        } else {
            realm.close();
            return false;
        }
    }

    public static void setSupporter(boolean supporter) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(getProduct(SKU_TWO_DOLLAR));
        realm.commitTransaction();
        realm.close();
    }
}
