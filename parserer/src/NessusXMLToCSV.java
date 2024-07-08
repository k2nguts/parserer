import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class NessusXMLToCSV {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
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

            // CSV dosyası oluşturma
            FileWriter csvWriter = new FileWriter("output.csv");
            csvWriter.append("Host IP,Plugin Name,Severity,Plugin ID,Port\n");

            // Tüm ReportHost öğelerini bulma
            NodeList reportHosts = rootElement.getElementsByTagName("ReportHost");

            // Her ReportHost öğesini işleme
            for (int i = 0; i < reportHosts.getLength(); i++) {
                Element reportHost = (Element) reportHosts.item(i);
                String hostName = reportHost.getAttribute("name");

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
                    pluginMap.get(pluginName).append(hostIP).append(",")
                            .append(pluginName).append(",")
                            .append(severity).append(",")
                            .append(pluginID).append(",")
                            .append(port).append("\n");
                }

                // Birleştirilmiş sonuçları CSV dosyasına yazdırma
                for (StringBuilder sb : pluginMap.values()) {
                    csvWriter.append(sb.toString());
                }
            }

            // CSV dosyasını kapatma
            csvWriter.flush();
            csvWriter.close();

            System.out.println("CSV dosyası başarıyla oluşturuldu: output.csv");

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
}
