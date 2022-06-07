package br.com.caelum.camel;

import com.thoughtworks.xstream.XStream;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.dataformat.xstream.XStreamDataFormat;
import org.apache.camel.impl.DefaultCamelContext;

import java.text.SimpleDateFormat;

public class RotaNegociacao {
    public static void main(String[] args) throws Exception {

        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure () throws Exception {
                final XStream xstream = new XStream();
                xstream.alias("negociacao", Negociacao.class);
                from("timer://negociacoes?fixedRate=true&delay=1s&period=360s").
                        to("http4://argentumws-spring.herokuapp.com/negociacoes").
                        unmarshal(new XStreamDataFormat(xstream)). //já tem
                        split(body()).  //já tem
                        process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Negociacao negociacao = exchange.getIn().getBody(Negociacao.class);
                        exchange.setProperty("preco", negociacao.getPreco());
                        exchange.setProperty("quantidade", negociacao.getQuantidade());
                        String data = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(negociacao.getData().getTime());
                        exchange.setProperty("data", data);
                    }
                }).
                        log("${body}").
                        end();
            }
        });

        context.start();
        Thread.sleep(20000);
        context.stop();
    }
}
