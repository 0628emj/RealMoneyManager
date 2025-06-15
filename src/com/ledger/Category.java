package com.ledger;

public enum Category {
    식비, 교통, 통신, 공과금, 쇼핑, 의료, 교육, 기타;

    public static String[] getNames() {
    	
        Category[] categories = values();
        String[] names = new String[categories.length];
        for (int i = 0; i < categories.length; i++) {
            names[i] = categories[i].name();
        }
        return names;
    }
    
    
}