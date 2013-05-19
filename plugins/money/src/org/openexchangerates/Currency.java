/*
 * Copyright 2013 Nebojsa Cvetkovic. All rights reserved.
 *
 * This file is part of JunglistIRC.
 *
 * JunglistIRC is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JunglistIRC is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JunglistIRC.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openexchangerates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;

/**
 * All available currencies on http://openexchangerates.org/currencies.json
 *
 * @author Demétrio Menezes Neto
 */
public enum Currency {
    /**
     * United Arab Emirates Dirham
     */
    AED,
    /**
     * Afghan Afghani
     */
    AFN,
    /**
     * Albanian Lek
     */
    ALL,
    /**
     * Armenian Dram
     */
    AMD,
    /**
     * Netherlands Antillean Guilder
     */
    ANG,
    /**
     * Angolan Kwanza
     */
    AOA,
    /**
     * Argentine Peso
     */
    ARS,
    /**
     * Australian Dollar
     */
    AUD,
    /**
     * Aruban Florin
     */
    AWG,
    /**
     * Azerbaijani Manat
     */
    AZN,
    /**
     * Bosnia-Herzegovina Convertible Mark
     */
    BAM,
    /**
     * Barbadian Dollar
     */
    BBD,
    /**
     * Bangladeshi Taka
     */
    BDT,
    /**
     * Bulgarian Lev
     */
    BGN,
    /**
     * Bahraini Dinar
     */
    BHD,
    /**
     * Burundian Franc
     */
    BIF,
    /**
     * Bermudan Dollar
     */
    BMD,
    /**
     * Brunei Dollar
     */
    BND,
    /**
     * Bolivian Boliviano
     */
    BOB,
    /**
     * Brazilian Real
     */
    BRL,
    /**
     * Bahamian Dollar
     */
    BSD,
    /**
     * Bitcoin
     */
    BTC,
    /**
     * Bhutanese Ngultrum
     */
    BTN,
    /**
     * Botswanan Pula
     */
    BWP,
    /**
     * Belarusian Ruble
     */
    BYR,
    /**
     * Belize Dollar
     */
    BZD,
    /**
     * Canadian Dollar
     */
    CAD,
    /**
     * Congolese Franc
     */
    CDF,
    /**
     * Swiss Franc
     */
    CHF,
    /**
     * Chilean Unit of Account (UF)
     */
    CLF,
    /**
     * Chilean Peso
     */
    CLP,
    /**
     * Chinese Yuan
     */
    CNY,
    /**
     * Colombian Peso
     */
    COP,
    /**
     * Costa Rican Colón
     */
    CRC,
    /**
     * Cuban Peso
     */
    CUP,
    /**
     * Cape Verdean Escudo
     */
    CVE,
    /**
     * Czech Republic Koruna
     */
    CZK,
    /**
     * Djiboutian Franc
     */
    DJF,
    /**
     * Danish Krone
     */
    DKK,
    /**
     * Dominican Peso
     */
    DOP,
    /**
     * Algerian Dinar
     */
    DZD,
    /**
     * Estonian Kroon
     */
    EEK,
    /**
     * Egyptian Pound
     */
    EGP,
    /**
     * Ethiopian Birr
     */
    ETB,
    /**
     * Euro
     */
    EUR,
    /**
     * Fijian Dollar
     */
    FJD,
    /**
     * Falkland Islands Pound
     */
    FKP,
    /**
     * British Pound Sterling
     */
    GBP,
    /**
     * Georgian Lari
     */
    GEL,
    /**
     * Ghanaian Cedi
     */
    GHS,
    /**
     * Gibraltar Pound
     */
    GIP,
    /**
     * Gambian Dalasi
     */
    GMD,
    /**
     * Guinean Franc
     */
    GNF,
    /**
     * Guatemalan Quetzal
     */
    GTQ,
    /**
     * Guyanaese Dollar
     */
    GYD,
    /**
     * Hong Kong Dollar
     */
    HKD,
    /**
     * Honduran Lempira
     */
    HNL,
    /**
     * Croatian Kuna
     */
    HRK,
    /**
     * Haitian Gourde
     */
    HTG,
    /**
     * Hungarian Forint
     */
    HUF,
    /**
     * Indonesian Rupiah
     */
    IDR,
    /**
     * Irish Pound
     */
    IEP,
    /**
     * Israeli New Sheqel
     */
    ILS,
    /**
     * Indian Rupee
     */
    INR,
    /**
     * Iraqi Dinar
     */
    IQD,
    /**
     * Iranian Rial
     */
    IRR,
    /**
     * Icelandic Króna
     */
    ISK,
    /**
     * Jersey Pound
     */
    JEP,
    /**
     * Jamaican Dollar
     */
    JMD,
    /**
     * Jordanian Dinar
     */
    JOD,
    /**
     * Japanese Yen
     */
    JPY,
    /**
     * Kenyan Shilling
     */
    KES,
    /**
     * Kyrgystani Som
     */
    KGS,
    /**
     * Cambodian Riel
     */
    KHR,
    /**
     * Comorian Franc
     */
    KMF,
    /**
     * North Korean Won
     */
    KPW,
    /**
     * South Korean Won
     */
    KRW,
    /**
     * Kuwaiti Dinar
     */
    KWD,
    /**
     * Cayman Islands Dollar
     */
    KYD,
    /**
     * Kazakhstani Tenge
     */
    KZT,
    /**
     * Laotian Kip
     */
    LAK,
    /**
     * Lebanese Pound
     */
    LBP,
    /**
     * Sri Lankan Rupee
     */
    LKR,
    /**
     * Liberian Dollar
     */
    LRD,
    /**
     * Lesotho Loti
     */
    LSL,
    /**
     * Lithuanian Litas
     */
    LTL,
    /**
     * Latvian Lats
     */
    LVL,
    /**
     * Libyan Dinar
     */
    LYD,
    /**
     * Moroccan Dirham
     */
    MAD,
    /**
     * Moldovan Leu
     */
    MDL,
    /**
     * Malagasy Ariary
     */
    MGA,
    /**
     * Macedonian Denar
     */
    MKD,
    /**
     * Myanma Kyat
     */
    MMK,
    /**
     * Mongolian Tugrik
     */
    MNT,
    /**
     * Macanese Pataca
     */
    MOP,
    /**
     * Mauritanian Ouguiya
     */
    MRO,
    /**
     * Maltese lira
     */
    MTL,
    /**
     * Mauritian Rupee
     */
    MUR,
    /**
     * Maldivian Rufiyaa
     */
    MVR,
    /**
     * Malawian Kwacha
     */
    MWK,
    /**
     * Mexican Peso
     */
    MXN,
    /**
     * Malaysian Ringgit
     */
    MYR,
    /**
     * Mozambican Metical
     */
    MZN,
    /**
     * Namibian Dollar
     */
    NAD,
    /**
     * Nigerian Naira
     */
    NGN,
    /**
     * Nicaraguan Córdoba
     */
    NIO,
    /**
     * Norwegian Krone
     */
    NOK,
    /**
     * Nepalese Rupee
     */
    NPR,
    /**
     * New Zealand Dollar
     */
    NZD,
    /**
     * Omani Rial
     */
    OMR,
    /**
     * Panamanian Balboa
     */
    PAB,
    /**
     * Peruvian Nuevo Sol
     */
    PEN,
    /**
     * Papua New Guinean Kina
     */
    PGK,
    /**
     * Philippine Peso
     */
    PHP,
    /**
     * Pakistani Rupee
     */
    PKR,
    /**
     * Polish Zloty
     */
    PLN,
    /**
     * Paraguayan Guarani
     */
    PYG,
    /**
     * Qatari Rial
     */
    QAR,
    /**
     * Romanian Leu
     */
    RON,
    /**
     * Serbian Dinar
     */
    RSD,
    /**
     * Russian Ruble
     */
    RUB,
    /**
     * Rwandan Franc
     */
    RWF,
    /**
     * Saudi Riyal
     */
    SAR,
    /**
     * Solomon Islands Dollar
     */
    SBD,
    /**
     * Seychellois Rupee
     */
    SCR,
    /**
     * Sudanese Pound
     */
    SDG,
    /**
     * Swedish Krona
     */
    SEK,
    /**
     * Singapore Dollar
     */
    SGD,
    /**
     * Saint Helena Pound
     */
    SHP,
    /**
     * Sierra Leonean Leone
     */
    SLL,
    /**
     * Somali Shilling
     */
    SOS,
    /**
     * Surinamese Dollar
     */
    SRD,
    /**
     * São Tomé and Príncipe Dobra
     */
    STD,
    /**
     * Salvadoran Colón
     */
    SVC,
    /**
     * Syrian Pound
     */
    SYP,
    /**
     * Swazi Lilangeni
     */
    SZL,
    /**
     * Thai Baht
     */
    THB,
    /**
     * Tajikistani Somoni
     */
    TJS,
    /**
     * Turkmenistani Manat
     */
    TMT,
    /**
     * Tunisian Dinar
     */
    TND,
    /**
     * Tongan Paʻanga
     */
    TOP,
    /**
     * Turkish Lira
     */
    TRY,
    /**
     * Trinidad and Tobago Dollar
     */
    TTD,
    /**
     * New Taiwan Dollar
     */
    TWD,
    /**
     * Tanzanian Shilling
     */
    TZS,
    /**
     * Ukrainian Hryvnia
     */
    UAH,
    /**
     * Ugandan Shilling
     */
    UGX,
    /**
     * United States Dollar
     */
    USD,
    /**
     * Uruguayan Peso
     */
    UYU,
    /**
     * Uzbekistan Som
     */
    UZS,
    /**
     * Venezuelan Bolívar
     */
    VEF,
    /**
     * Vietnamese Dong
     */
    VND,
    /**
     * Vanuatu Vatu
     */
    VUV,
    /**
     * Samoan Tala
     */
    WST,
    /**
     * CFA Franc BEAC
     */
    XAF,
    /**
     * Silver ounce
     */
    XAG,
    /**
     * Gold ounce
     */
    XAU,
    /**
     * East Caribbean Dollar
     */
    XCD,
    /**
     * Special Drawing Rights
     */
    XDR,
    /**
     * CFA Franc BCEAO
     */
    XOF,
    /**
     * CFP Franc
     */
    XPF,
    /**
     * Yemeni Rial
     */
    YER,
    /**
     * South African Rand
     */
    ZAR,
    /**
     * Zambian Kwacha
     */
    ZMK,
    /**
     * Zambian Kwacha
     */
    ZMW,
    /**
     * Zimbabwean Dollar (1980-2008)
     */
    ZWD,
    /**
     * Zimbabwean Dollar
     */
    ZWL;

    public static class Deserializer implements JsonDeserializer<Currency> {
        @Override
        public Currency deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return Arrays.stream(Currency.values())
                    .filter((currency) -> currency.name().equals(jsonElement.getAsString()))
                    .findAny().orElse(null);
        }
    }
}
