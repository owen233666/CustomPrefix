package cn.owen233666.customPrefix.prefixdisplay;

public class FormatCodeTranslation {

    public static String ProcessFormatCode(String s) {
        return insertClosingTag(s);
    }

    private static String insertClosingTag(String input) {
        /**
         *  定义需要处理的格式代码
         *  &l为粗体
         *  &k为混乱字符
         *  &m为删除线
         *  &n为下划线
         *  &o为斜体
         */
        String[] formatCodes = {"&l", "&k", "&m", "&o", "&n"};
        // 遍历每个格式代码
        for (String code : formatCodes) {
            int index = 0;
            while ((index = input.indexOf(code, index)) != -1) {
                // 找到格式代码的结束位置
                int endIndex = input.indexOf("&", index + code.length());
                // 在下一个 & 之前插入关闭标签
                if(code.equalsIgnoreCase("&l")){
                    String closingTag = "</b>";
                    input = input.substring(0, endIndex) + closingTag + input.substring(endIndex);
                    index = endIndex + closingTag.length(); // 更新索引，避免重复处理
                }
                if(code.equalsIgnoreCase("&o")){
                    String closingTag = "</i>";
                    input = input.substring(0, endIndex) + closingTag + input.substring(endIndex);
                    index = endIndex + closingTag.length(); // 更新索引，避免重复处理
                }
                if(code.equalsIgnoreCase("&k")){
                    String closingTag = "</obf>";
                    input = input.substring(0, endIndex) + closingTag + input.substring(endIndex);
                    index = endIndex + closingTag.length(); // 更新索引，避免重复处理
                }
                if(code.equalsIgnoreCase("&n")){
                    String closingTag = "</u>";
                    input = input.substring(0, endIndex) + closingTag + input.substring(endIndex);
                    index = endIndex + closingTag.length(); // 更新索引，避免重复处理
                }
                if(code.equalsIgnoreCase("&m")){
                    String closingTag = "</st>";
                    input = input.substring(0, endIndex) + closingTag + input.substring(endIndex);
                    index = endIndex + closingTag.length(); // 更新索引，避免重复处理
                }
            }
        }
        return input;
    }
}
