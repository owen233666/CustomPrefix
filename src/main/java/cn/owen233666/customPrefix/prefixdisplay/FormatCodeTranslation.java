package cn.owen233666.customPrefix.prefixdisplay;

public class FormatCodeProcessor {

    public static String ProcessFormatCode(String s) {
        return insertClosingTag(s);
    }

    /**
     * 在指定的格式代码后插入关闭标签
     *
     * @param input       输入字符串
     * @param closingTag  要插入的关闭标签（例如 </b>）
     * @return 处理后的字符串
     */
    public static String insertClosingTag(String input) {
        /** 定义需要处理的格式代码
         *
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
