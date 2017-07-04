import amf.builder.WebApiBuilder;
import amf.model.WebApiModel;

import java.util.Arrays;
import java.util.List;

public class TestMainClass {

    public static void main(String[] args){
         System.out.println("write here your code for test the web api :)");
        List<String> list = Arrays.asList("one","two");
        WebApiBuilder builder = new WebApiBuilder();
        builder.build().toBuilder().withScheme(list);
        WebApiModel webApi = builder.build();
        System.out.println("Web api name: "+webApi.name()+" list to string: "+webApi.schemeList());
    }
}
