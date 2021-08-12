package cn.qd.willie.mysocket;

public class ResponseModel {

    /**
     * title : 脊灰灭活疫苗知情同意书
     * content : PHA+
     * vaccineNm : 脊灰灭活疫苗（Salk株）
     * manufacturerNm : 成都生物
     */

    private String title;
    private String content;
    private String vaccineNm;
    private String manufacturerNm;
    private String contentHtml;

    public String getContentHtml() {
        return contentHtml;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVaccineNm() {
        return vaccineNm;
    }

    public void setVaccineNm(String vaccineNm) {
        this.vaccineNm = vaccineNm;
    }

    public String getManufacturerNm() {
        return manufacturerNm;
    }

    public void setManufacturerNm(String manufacturerNm) {
        this.manufacturerNm = manufacturerNm;
    }
}
