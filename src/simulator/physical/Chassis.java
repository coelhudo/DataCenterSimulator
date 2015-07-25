package simulator.physical;

import java.util.ArrayList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Chassis {

    public ArrayList<BladeServer> servers = new ArrayList<BladeServer>();
    boolean turnON = true;
    public int chassisID, rackId;
    String chassisType = new String();

    public Chassis(int idArg) {
        //if it is -1 means this chassis is just a template and not assigned yet
        chassisID = idArg;
    }

    public void turnIt(boolean tag) {
        turnON = tag;
    }

    public boolean isReady() {
        int RDY = 0;
        for (int i = 0; i < servers.size(); i++) {
            RDY = RDY + servers.get(i).getReady();
        }
        if (RDY == 0) {
            return false;
        } else {
            return true;
        }
    }

    double power() {
        double pw = 0;
        int i;
        for (i = 0; i < servers.size(); i++) {
            pw = pw + servers.get(i).getPower();
        }
        //pw=(cpus*a/100)+w*servers.size();
        //System.out.println("powercost=     " + (int)pw+"\t"+cpus);
        return pw;
    }

    void readFromNode(Node node) {
        servers.clear();
        int[] number = null;
        String[] s = null;

        int tedad = 0;
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ChassisType")) {
                    chassisType = (childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("numberOfBladeServer")) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(" ");
                    number = new int[split.length];
                    for (int j = 0; j < split.length; j++) {
                        number[j] = Integer.parseInt(split[j]);
                    }
                    tedad = number.length;
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("BladeType")) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    //s=str.split("\"");
                    String[] split = str.split(",");
                    s = new String[split.length];
                    System.arraycopy(split, 0, s, 0, split.length);
                }
            }
        }
        for (int j = 0; j < tedad; j++) {
            for (int k = 0; k < number[j]; k++) {
                BladeServer bldServ = new BladeServer(-1);
                //s[j]=s[j].substring(1,s[j].length()-1);
                bldServ.bladeType = s[j].trim();
                servers.add(bldServ);
            }
        }

//                if(childNodes.item(i).getNodeName().equalsIgnoreCase("bladeServer"))
//                {
//                    BladeServer bladeServer = new BladeServer(chassisID);
//
//                    bladeServer.readFromNode(childNodes.item(i));
//                    servers.add(bladeServer);
//                }
    }
}
