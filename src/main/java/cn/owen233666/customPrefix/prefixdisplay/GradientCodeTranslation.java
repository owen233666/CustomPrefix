package cn.owen233666.customPrefix.prefixdisplay;

public class GradientCodeTranslation {
    public static String TranslatetoGradient(String s){
        return s.replace("<g:#", "<gradient:#");
    }
}
