package simulator.physical;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import simulator.Environment;

public class Chassis {

    private List<BladeServer> servers = new ArrayList<BladeServer>();
    private int chassisID, rackId;
    private String chassisType = new String();
    private Environment environment;

    public Chassis(int idArg, Environment environment) {
        // if it is -1 means this chassis is just a template and not assigned
        // yet
        chassisID = idArg;
        this.environment = environment;
    }
    
    public List<BladeServer> getServers() {
        return servers;
    }
    
    public int getRackID() {
        return rackId;
    }
    
    public void setRackID(int rackId) {
        this.rackId = rackId;
    }

    public int getChassisID() {
        return chassisID;
    }
    
    public boolean isReady() {
        int RDY = 0;
        for (BladeServer bladeServer : servers) {
            RDY = RDY + bladeServer.getReady();
        }
        if (RDY == 0) {
            return false;
        } else {
            return true;
        }
    }

    double power() {
        double pw = 0;
        for (BladeServer bladeServer : servers) {
            pw = pw + bladeServer.getPower();
        }
        // pw=(cpus*a/100)+w*servers.size();
        // LOGGER.info("powercost= " + (int)pw+"\t"+cpus);
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
                    setChassisType((childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
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
                    // s=str.split("\"");
                    String[] split = str.split(",");
                    s = new String[split.length];
                    System.arraycopy(split, 0, s, 0, split.length);
                }
            }
        }
        for (int j = 0; j < tedad; j++) {
            for (int k = 0; k < number[j]; k++) {
                BladeServerPOD bladeServerPOD = new BladeServerPOD();
                bladeServerPOD.setBladeType(s[j].trim());
                BladeServer bldServ = new BladeServer(bladeServerPOD, -1, environment);
                // s[j]=s[j].substring(1,s[j].length()-1);
                servers.add(bldServ);
            }
        }

        // if(childNodes.item(i).getNodeName().equalsIgnoreCase("bladeServer"))
        // {
        // BladeServer bladeServer = new BladeServer(chassisID);
        //
        // bladeServer.readFromNode(childNodes.item(i));
        // servers.add(bladeServer);
        // }
    }

    protected String getChassisType() {
        return chassisType;
    }

    private void setChassisType(String chassisType) {
        this.chassisType = chassisType;
    }
}
