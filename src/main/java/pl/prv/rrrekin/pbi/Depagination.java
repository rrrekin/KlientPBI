/*
 * Copyright (C) 2014 Ericsson Sp. z o.o.
 *
 * All rights reserved.
 * The Copyright to the computer program(s) herein is the property of
 * Ericsson Sp. z o.o.
 * The program(s) may be used and/or copied with the written permission
 * from Ericsson Sp. z o.o. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *
 * The contents of this document are subject to revision without notice due
 * to continued progress in methodology, design and manufacturing.
 *
 * Ericsson shall have no liability for any errors or damage of any kind
 * resulting from the use of modified versions of this document.
 */
package pl.prv.rrrekin.pbi;

import java.util.ResourceBundle;

/**
 *
 * @author Michał Rudewicz
 */
public enum Depagination {

    NONE, VERSE, STANDARD, DEHYPHENATE;

    private static final ResourceBundle guiTexts = ResourceBundle.getBundle("pl/prv/rrrekin/pbi/gui");
    private final static String[] names;

    static {
        names = new String[values().length];
        for (int i = 0; i < names.length; i++) {
            names[i] = guiTexts.getString("E_"+values()[i].name());
        }
    }

    public static String[] localValues() {
        return names.clone();
    }

    static Depagination fromLocalName(String val) {
        for(int i=0;i<names.length;i++){
            if(names[i].equals(val))return values()[i];
        }
        return Depagination.STANDARD;
    }

    @Override
    public String toString() {
        return names[ordinal()];
    }
    
    

}
