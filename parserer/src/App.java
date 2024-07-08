import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;



public class App {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);//"C:/Users/kaana/OneDrive/Masaüstü/EXT(2).nessus"
        System.out.print("Dosya yolunu giriniz ");
        String x = input.nextLine();

        try {
            // XML dosyasını yükleme
            File xmlFile = new File(x);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            // Kök elemanı seçme
            Element rootElement = doc.getDocumentElement();

            // Tüm ReportHost öğelerini bulma
            NodeList reportHosts = rootElement.getElementsByTagName("ReportHost");

            // Her ReportHost öğesini işleme
            for (int i = 0; i < reportHosts.getLength(); i++) {
                Element reportHost = (Element) reportHosts.item(i);
                String hostName = reportHost.getAttribute("name");
                System.out.println("ReportHost: " + hostName);

                // HostProperties içindeki host-ip öğesini bulma
                NodeList hostIPList = reportHost.getElementsByTagName("tag");
                String hostIP = "";
                for (int j = 0; j < hostIPList.getLength(); j++) {
                    Element tag = (Element) hostIPList.item(j);
                    String tagName = tag.getAttribute("name");
                    if ("host-ip".equals(tagName)) {
                        hostIP = tag.getTextContent().trim();
                        break; // İlk bulduğumuz host-ip çıkış yap
                    }
                }

                // ReportItem öğelerini bulma
                NodeList reportItems = reportHost.getElementsByTagName("ReportItem");
                List<Element> filteredReportItems = new ArrayList<>();

                // Severity değeri 0 olmayanları filtreleme
                for (int k = 0; k < reportItems.getLength(); k++) {
                    Element reportItem = (Element) reportItems.item(k);
                    String severity = reportItem.getAttribute("severity");
                    if (!"0".equals(severity)) {
                        filteredReportItems.add(reportItem);
                    }
                }

                // Severity değerine göre sıralama
                Collections.sort(filteredReportItems, new Comparator<Element>() {
                    @Override
                    public int compare(Element o1, Element o2) {
                        int severity1 = Integer.parseInt(o1.getAttribute("severity"));
                        int severity2 = Integer.parseInt(o2.getAttribute("severity"));
                        // Büyükten küçüğe sıralama
                        return Integer.compare(severity2, severity1);
                    }
                });

                // Plugin Name'leri aynı olanları bir satırda yazdırma için kullanılacak map
                Map<String, StringBuilder> pluginMap = new HashMap<>();

                // Sıralanmış ReportItem'ları işleme
                for (Element reportItem : filteredReportItems) {
                    String severity = reportItem.getAttribute("severity");
                    String pluginID = reportItem.getAttribute("pluginID");
                    String pluginName = reportItem.getAttribute("pluginName");
                    String port = reportItem.getAttribute("port");

                    // Plugin Name'e göre map'te biriktirme
                    if (!pluginMap.containsKey(pluginName)) {
                        pluginMap.put(pluginName, new StringBuilder());
                    }
                    pluginMap.get(pluginName).append("Severity: ").append(severity)
                            .append(", Plugin ID: ").append(pluginID)
                            .append(", Plugin Name: ").append(pluginName)
                            .append(", Port: ").append(port)
                            .append(", Host IP: ").append(hostIP).append("\n");
                }

                // Birleştirilmiş sonuçları yazdırma
                for (StringBuilder sb : pluginMap.values()) {
                    System.out.println(sb.toString().trim());
                }

                System.out.println(); // ReportHost arasında boşluk bırak
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
}
