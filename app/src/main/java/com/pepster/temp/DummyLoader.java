package com.pepster.temp;

import com.firebase.client.utilities.PushIdGenerator;
import com.pepster.utilities.AdaptiveLocation;
import com.pepster.data.MapContent;
import com.pepster.data.PepPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by WinNabuska on 12.3.2016.
 */
public class DummyLoader {
    private final static String TAG = DummyLoader.class.getSimpleName();

    private static final Random random = new Random();

    public static List<AdaptiveLocation> wayPoints1 = Arrays.asList(
            new AdaptiveLocation(60.19007600, 25.03298430), new AdaptiveLocation(60.19020940, 25.03314520),
            new AdaptiveLocation(60.19082270, 25.03273750), new AdaptiveLocation(60.19124400, 25.03250680),
            new AdaptiveLocation(60.19193740, 25.03214210), new AdaptiveLocation(60.19295600, 25.03146610),
            new AdaptiveLocation(60.19300930, 25.03182020), new AdaptiveLocation(60.19261740, 25.03338120),
            new AdaptiveLocation(60.19203600, 25.03272140), new AdaptiveLocation(60.19194540, 25.03224400),
            new AdaptiveLocation(60.19187870, 25.03083850), new AdaptiveLocation(60.19171070, 25.03014110),
            new AdaptiveLocation(60.19101740, 25.03072580), new AdaptiveLocation(60.19007330, 25.03290380)
    );

    public static List<PepPoint> pepPoints1 = Arrays.asList(
            new PepPoint(new AdaptiveLocation(60.190229, 25.033157), "koti", 25, PepPoint.TYPE_CUSTOM),
            new PepPoint(new AdaptiveLocation(60.190824, 25.032685), "herttoniemen koulu", 25, PepPoint.TYPE_CUSTOM),
            new PepPoint(new AdaptiveLocation(60.193018, 25.031547), "kahvila", 25, PepPoint.TYPE_CUSTOM),
            new PepPoint(new AdaptiveLocation(60.192945, 25.032440), "Liekki 1", 25, PepPoint.TYPE_CUSTOM),
            new PepPoint(new AdaptiveLocation(60.190817, 25.031019), "urheilu kenttä", 25, PepPoint.TYPE_CUSTOM)
    );

    public static List<AdaptiveLocation> wayPoints2 = Arrays.asList(
            new AdaptiveLocation(60.19015073, 25.03316938), new AdaptiveLocation(60.19351070, 25.03078758),
            new AdaptiveLocation(60.19357469, 25.03094047), new AdaptiveLocation(60.19403866, 25.03006070),
            new AdaptiveLocation(60.19423598, 25.02957791), new AdaptiveLocation(60.19437996, 25.02988904),
            new AdaptiveLocation(60.19366535, 25.02792298), new AdaptiveLocation(60.19291873, 25.02410352),
            new AdaptiveLocation(60.19232675, 25.02198994), new AdaptiveLocation(60.19065208, 25.01817047),
            new AdaptiveLocation(60.18990005, 25.01592814), new AdaptiveLocation(60.18905200, 25.01045644),
            new AdaptiveLocation(60.18863597, 25.00588595), new AdaptiveLocation(60.18818259, 25.00327885),
            new AdaptiveLocation(60.18744117, 25.00091850), new AdaptiveLocation(60.18666774, 24.99817192),
            new AdaptiveLocation(60.18605964, 24.99197065), new AdaptiveLocation(60.18634235, 24.98687446),
            new AdaptiveLocation(60.18699845, 24.98104870), new AdaptiveLocation(60.18789456, 24.97403204),
            new AdaptiveLocation(60.18878531, 24.96926844), new AdaptiveLocation(60.18907333, 24.96464967),
            new AdaptiveLocation(60.18725982, 24.95997190), new AdaptiveLocation(60.18286429, 24.95061635),
            new AdaptiveLocation(60.17697421, 24.95010137), new AdaptiveLocation(60.17065614, 24.94113206),
            new AdaptiveLocation(60.16832925, 24.92991507), new AdaptiveLocation(60.16764610, 24.93083775),
            new AdaptiveLocation(60.16664802, 24.93059098), new AdaptiveLocation(60.16585808, 24.93099868),
            new AdaptiveLocation(60.16463043, 24.93279039), new AdaptiveLocation(60.16437422, 24.93296205),
            new AdaptiveLocation(60.16371768, 24.93114888), new AdaptiveLocation(60.16331733, 24.93159949)
    );
    public static List<PepPoint> pepPoints2 = Arrays.asList(
            new PepPoint(new AdaptiveLocation(60.191251, 25.032446), "continue forward", 25, PepPoint.TYPE_DIRECTION),
            new PepPoint(new AdaptiveLocation(60.192995, 25.031190), "varo kerjäläistä", 25, PepPoint.TYPE_CUSTOM),
            new PepPoint(new AdaptiveLocation(60.194273, 25.029635), "lataa kautta", 25, PepPoint.TYPE_CUSTOM),
            new PepPoint(new AdaptiveLocation(60.194603, 25.030331), "lännen suuntaan", 25, PepPoint.TYPE_CUSTOM),
            new PepPoint(new AdaptiveLocation(60.188923, 24.967440), "pysy hereillä", 200, PepPoint.TYPE_CUSTOM),
            new PepPoint(new AdaptiveLocation(60.170754, 24.940533), "herätys", 50, PepPoint.TYPE_CUSTOM),
            new PepPoint(new AdaptiveLocation(60.169155, 24.932358), "nouse metrosta", 50, PepPoint.TYPE_CUSTOM),
            new PepPoint(new AdaptiveLocation(60.167099, 24.930798), "En svart anal tapp i 30 påengs rabat. Se på clockan tre", 10, PepPoint.TYPE_CUSTOM),
            new PepPoint(new AdaptiveLocation(60.166554, 24.930427), "Älä jää dösän alle", 5, PepPoint.TYPE_CUSTOM),
            new PepPoint(new AdaptiveLocation(60.165821, 24.931050), "odota kiltisti vihreitä", 10, PepPoint.TYPE_CUSTOM),
            new PepPoint(new AdaptiveLocation(60.165300, 24.931827), "Walk like a man, fastest you can", 15, PepPoint.TYPE_CUSTOM),
            new PepPoint(new AdaptiveLocation(60.164505, 24.932945), "olet jo 5 min myöhässä", 20, PepPoint.TYPE_CUSTOM),
            new PepPoint(new AdaptiveLocation(60.163530, 24.931155), "joku vois tehdä äpin joka kertoo automaattisesti koululle tullessa missä luokassa tunti on", 15,PepPoint.TYPE_CUSTOM)
    );


    //TODO removeFirst
    public static MapContent getDummyPepMapContent() {
        MapContent dummyContent = new MapContent();
        dummyContent.setTitle("Koti reitti");
        dummyContent.setModTime(1458310618447L + 10132);
        dummyContent.setLastUsage(1458310619447L + 53121);

        List<String> ids = new ArrayList<>();
        dummyContent.route().addAll(new ArrayList<>(wayPoints1));
        for (int i = 0; i<pepPoints1.size(); i++){
            PepPoint pp  = pepPoints1.get(i);
            pp.setLanguage("fi_FI");
            pp.isActive(true);
            pp.setType(PepPoint.TYPE_CUSTOM);
            pp.setPreconditions(ids);
            ids.add(PushIdGenerator.generatePushChildName(System.nanoTime() + random.nextInt(Integer.MAX_VALUE)));
        }
        Map<String, PepPoint> contens = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            contens.put(ids.get(i), pepPoints1.get(i));
        }
        dummyContent.setPepPoints(contens);
        return dummyContent;
    }



    public static MapContent getDummyPepMapContent2() {
        MapContent dummyContent = new MapContent();
        dummyContent.setTitle("Arjen parkour");
        dummyContent.setLastUsage(System.currentTimeMillis()-1000000);
        dummyContent.setModTime(System.currentTimeMillis());
        dummyContent.route().addAll(new ArrayList<>(wayPoints2));
        List<String> ids = new ArrayList<>();
        for (int i = 0; i<pepPoints2.size(); i++){
            PepPoint pp  = pepPoints2.get(i);
            pp.setLanguage("fi_FI");
            pp.isActive(true);
            pp.setType(PepPoint.TYPE_CUSTOM);
            pp.setPreconditions(ids);
            pp.setPreconditions(ids);
            ids.add(PushIdGenerator.generatePushChildName(System.nanoTime() + random.nextInt(Integer.MAX_VALUE)));
        }
        pepPoints2.get(2).isActive(false);
        pepPoints2.get(0).setLanguage("en_US");
        pepPoints2.get(7).getPreconditions().add(ids.get(6));
        pepPoints2.get(7).getPreconditions().add(ids.get(1));
        pepPoints2.get(7).setLanguage("sv_SE");
        pepPoints2.get(10).setLanguage("en_US");

        Map<String, PepPoint> contens = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            contens.put(ids.get(i), pepPoints2.get(i));
        }
        dummyContent.setPepPoints(contens);
        return dummyContent;
    }


    /*Supported locales [ar, ar_EG, bg, bg_BG, ca, ca_ES, cs, cs_CZ, da, da_DK, de, de_AT, de_BE, de_CH, de_DE, de_LI, de_LU, el, el_CY, el_GR, en, en
_AU, en_BE, en_BW, en_BZ, en_CA, en_GB, en_HK, en_IE, en_IN, en_JM, en_MH, en_MT, en_NA, en_NZ, en_PH, en_PK, en_RH, en_SG, en_TT, en_US, en_US_POSIX,
 en_VI, en_ZA, en_ZW, es, es_AR, es_BO, es_CL, es_CO, es_CR, es_DO, es_EC, es_ES, es_GT, es_HN, es_MX, es_NI, es_PA, es_PE, es_PR, es_PY, es_SV, es_US
, es_UY, es_VE, et, et_EE, eu, eu_ES, fa, fa_IR, fi, fi_FI, fr, fr_BE, fr_CA, fr_CH, fr_FR, fr_LU, fr_MC, gl, gl_ES, hr, hr_HR, hu, hu_HU, in, in_ID,
is, is_IS, it, it_CH, it_IT, iw, iw_IL, ja, ja_JP, kk, kk_KZ, ko, ko_KR, lt, lt_LT, lv, lv_LV, mk, mk_MK, ms, ms_BN, ms_MY, nl, nl_BE, nl_NL, no, no_N
O, no_NO_NY, pl, pl_PL, pt, pt_BR, pt_PT, ro, ro_RO, ru, ru_RU, ru_UA, sh, sh_BA, sh_CS, sh_YU, sk, sk_SK, sl, sl_SI, sq, sq_AL, sr, sr_BA, sr_ME, sr_
RS, sv, sv_FI, sv_SE, th, th_TH, tr, tr_TR, uk, uk_UA, vi, vi_VN, zh, zh_CN, zh_HK, zh_HANS_SG, zh_HANT_MO, zh_MO, zh_TW]*/

}
