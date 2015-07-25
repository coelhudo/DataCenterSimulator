package simulator;

import simulator.physical.DataCenter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class Simulator {

	private static Simulator instance = null;

	public static Simulator getInstance() {
		if (instance == null) {
			instance = new Simulator();
		}
		return instance;

	}

	private void run() throws IOException {
		///////////////////////
		while (!anySystem()) {
			// System.out.println("--"+Main.localTime);
			allSystemRunACycle();
			allSystemCalculatePwr();
			datacenter.calculatePower();
			localTime++;
			// ////Data Center Level AM MAPE Loop
			// if(Main.localTime%1==0)
			// {
			// mesg++;
			// DataCenter.AM.monitor();
			// DataCenter.AM.analysis(0);
			// }
			// ///////////////
		}
	}

	private Simulator() {
	}

	private void initialize(String config) {
		CreatLogicalDC(config);

		try {
			SLALogE = new OutputStreamWriter(new FileOutputStream(new File("slaViolLogE.txt")));
			SLALogI = new OutputStreamWriter(new FileOutputStream(new File("slaViolLogI.txt")));
			SLALogH = new OutputStreamWriter(new FileOutputStream(new File("slaViolLogH.txt")));
		} catch (IOException e) {
			System.out.println("Uh oh, got an IOException error!" + e.getMessage());
		} finally {
		}

		// set the overal policy here
		// Data Center is green!
		datacenter.getAM().setStrategy(StrategyEnum.Green);
		// CS.get(0).AM.strtg=strategyEnum.SLA;
		// CS.get(1).AM.strtg=strategyEnum.Green;

	}

	public int localTime = 1;
	public int mesg = 0, mesg2 = 0;
	public int epochApp = 60, epochSys = 120, epochSideApp = 120;
	public ArrayList<ResponseTime> responseArray;
	public ArrayList<InteractiveSystem> IS = new ArrayList<InteractiveSystem>();
	public ArrayList<EnterpriseSystem> ES = new ArrayList<EnterpriseSystem>();
	public ArrayList<ComputeSystem> CS = new ArrayList<ComputeSystem>();
	public double[] peakEstimate;
	private OutputStreamWriter SLALogE = null;
	private OutputStreamWriter SLALogI = null;
	private OutputStreamWriter SLALogH = null;
	public int communicationAM = 0;
	private DataCenter datacenter;

	private double getTotalPowerConsumption() {
		return datacenter.totalPowerConsumption;
	}

	private int getOverRedTempNumber() {
		return datacenter.getOverRed();
	}

	public void logHpcViolation(String name, Violation slaViolation) {
		try {
			SLALogH.write(name + "\t" + Simulator.getInstance().localTime + "\t" + slaViolation + "\n");
		} catch (IOException ex) {
			Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void logEnterpriseViolation(String name, int slaViolationNum) {
		try {
			SLALogE.write(name + "\t" + Simulator.getInstance().localTime + "\t" + slaViolationNum + "\n");
		} catch (IOException ex) {
			Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void logInteractiveViolation(String name, int slaViolation) {
		try {
			SLALogI.write(name + "\t" + Simulator.getInstance().localTime + "\t" + slaViolation + "\n");
		} catch (IOException ex) {
			Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public DataCenter getDatacenter() {
		return datacenter;
	}

	public enum StrategyEnum {

		Green, SLA
	};

	public boolean anySysetm() {
		for (int i = 0; i < ES.size(); i++) {
			if (!ES.get(i).sysIsDone) {
				return false;
			}
		}
		for (int i = 0; i < IS.size(); i++) {
			if (!IS.get(i).sysIsDone) {
				return false;
			}
		}
		for (int i = 0; i < CS.size(); i++) {
			if (!CS.get(i).sysIsDone) {
				return false; // still we have work to do
			}
		}
		return true; // there is no job left in all system
	}

	public void CreatLogicalDC(String config) {
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			final File file = new File(config);
			Document doc = docBuilder.parse(file);
			String path = file.getParent();
			// normalize text representation
			doc.getDocumentElement().normalize();
			Node node = doc.getDocumentElement();
			NodeList childNodes = node.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					if (childNodes.item(i).getNodeName().equalsIgnoreCase("layout")) {
						String DCLayout = path + "/" + childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
						datacenter = new DataCenter(DCLayout);
					}
					if (childNodes.item(i).getNodeName().equalsIgnoreCase("System")) {
						NodeList nodiLst = childNodes.item(i).getChildNodes();
						systemConfig(nodiLst, path);
					}
				}
			}

		} catch (ParserConfigurationException ex) {
			Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
		} catch (SAXException ex) {
			Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void systemConfig(NodeList nodiLst, String path) {
		int whichSystem = -1;
		// whichSystem=1 means Enterprise
		// whichSystem=2 means Interactive
		// whichSystem=3 means HPC
		String name = new String();
		for (int i = 0; i < nodiLst.getLength(); i++) {
			if (nodiLst.item(i).getNodeType() == Node.ELEMENT_NODE) {
				if (nodiLst.item(i).getNodeName().equalsIgnoreCase("type")) {
					String systemType = nodiLst.item(i).getChildNodes().item(0).getNodeValue().trim();
					if (systemType.equalsIgnoreCase("Enterprise")) {
						whichSystem = 1;
					} else if (systemType.equalsIgnoreCase("Interactive")) {
						whichSystem = 2;
					} else if (systemType.equalsIgnoreCase("HPC")) {
						whichSystem = 3;
					}
				}
				if (nodiLst.item(i).getNodeName().equalsIgnoreCase("name")) {
					name = nodiLst.item(i).getChildNodes().item(0).getNodeValue().trim();
				}
				if (nodiLst.item(i).getNodeName().equalsIgnoreCase("configFile")) {
					String fileName = path + "/" + nodiLst.item(i).getChildNodes().item(0).getNodeValue().trim();
					switch (whichSystem) {
					case 1:
						System.out.println("------------------------------------------");
						System.out.println("Initialization of Enterprise System Name=" + name);
						EnterpriseSystem ES1 = new EnterpriseSystem(fileName);
						ES1.name = name;
						ES.add(ES1);
						whichSystem = -1;
						break;
					case 2:
						System.out.println("------------------------------------------");
						System.out.println("Initialization of Interactive System Name=" + name);
						InteractiveSystem wb1 = new InteractiveSystem(fileName);
						wb1.name = name;
						IS.add(wb1);
						whichSystem = -1;
						break;
					case 3:
						System.out.println("------------------------------------------");
						System.out.println("Initialization of HPC System Name=" + name);
						ComputeSystem CP = new ComputeSystem(fileName);
						CP.name = name;
						CS.add(CP);
						whichSystem = -1;
						break;
					}
				}
			}
		}
	}

	public void allSystemRunACycle() throws IOException {
		for (int i = 0; i < ES.size(); i++) {
			if (!ES.get(i).sysIsDone) {
				ES.get(i).runAcycle();
			}
		}
		for (int i = 0; i < CS.size(); i++) {
			if (!CS.get(i).sysIsDone) {
				CS.get(i).runAcycle();
			}
		}
		for (int i = 0; i < IS.size(); i++) {
			if (!IS.get(i).sysIsDone) {
				IS.get(i).runAcycle();
			}
		}
	}
	/////////////////////////////

	public void allSystemCalculatePwr() throws IOException {
		for (int i = 0; i < ES.size(); i++) {
			ES.get(i).calculatePwr();
		}
		for (int i = 0; i < CS.size(); i++) {
			CS.get(i).calculatePwr();
		}
		for (int i = 0; i < IS.size(); i++) {
			IS.get(i).calculatePwr();
		}
	}
	/////////////////////////////

	public void GetStat() {
		for (int i = 0; i < 50; i++) {
			datacenter.chassisSet.get(i).servers.get(0).ready = -1;
			datacenter.chassisSet.get(i).servers.get(0).Mips = 1;// 1.04 1.4;
			datacenter.chassisSet.get(i).servers.get(0).currentCPU = 100;
		}
		datacenter.calculatePower();
	}

	public static void main(String[] args) throws IOException {
		Simulator simulator = Simulator.getInstance();
		simulator.initialize("configs/DC_Logic.xml");
		System.out.println("------------------------------------------");
		System.out.println("Systems start running");
		System.out.println("------------------------------------------");
		simulator.run();
		System.out.println("------------------------------------------");
		simulator.csFinalize();
		// System.out.println("Total JOBs= "+CS.totalJob);
		System.out.println("Total energy Consumption= " + simulator.getTotalPowerConsumption());
		System.out.println("LocalTime= " + simulator.localTime);
		System.out.println("Mean Power Consumption= " + simulator.getTotalPowerConsumption() / simulator.localTime);
		System.out.println("Over RED\t " + simulator.getOverRedTempNumber() + "\t# of Messages DC to sys= "
				+ simulator.mesg + "\t# of Messages sys to nodes= " + simulator.mesg2);
	}

	void csFinalize() {
		for (int i = 0; i < CS.size(); i++) {
			System.out.println("Total Response Time in CS " + i + "th CS = " + CS.get(i).finalized());
		}
		try {
			datacenter.shutDownDC();
			SLALogE.close();
			SLALogH.close();
			SLALogI.close();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	public boolean anySystem() {
		boolean retValue = true;
		for (int i = 0; i < ES.size(); i++) {
			if (ES.get(i).sysIsDone == false) {
				retValue = false;
			} else {
				System.out.println("--------------------------------------");
				System.out.println("finishing Time EnterSys: " + ES.get(i).name + " at time: " + localTime);
				System.out.println("Computing Power Consumed by  " + ES.get(i).name + " is: " + ES.get(i).pwr);
				// System.out.println("Number of violation:
				// "+ES.get(i).accumolatedViolation);

				ES.remove(i);
				i--;
			}
		}
		for (int i = 0; i < IS.size(); i++) {
			if (IS.get(i).sysIsDone == false) {
				retValue = false;
			} else {
				System.out.println("--------------------------------------");
				System.out.println("finishing Time Interactive sys:  " + IS.get(i).name + " at time: " + localTime);
				System.out.println("Interactive sys: Number of violation: " + IS.get(i).accumolatedViolation);
				System.out.println("Computing Power Consumed by  " + IS.get(i).name + " is: " + IS.get(i).pwr);
				IS.remove(i);
				i--;

				// opps !! hardcoded policy
				datacenter.getAM().resetBlockTimer();
			}
		}
		for (int i = 0; i < CS.size(); i++) {
			if (CS.get(i).sysIsDone == false) {
				retValue = false; // means still we have work to do
			} else {
				System.out.println("--------------------------------------");
				System.out.println("finishing Time HPC_Sys:  " + CS.get(i).name + " at time: " + localTime);
				System.out.println("Total Response Time= " + CS.get(i).finalized());
				System.out.println("Number of violation HPC : " + CS.get(i).accumolatedViolation);
				System.out.println("Computing Power Consumed by  " + CS.get(i).name + " is: " + CS.get(i).pwr);
				CS.remove(i);
				i--;
			}
		}
		return retValue; // there is no job left in all system
	}
	// void getPeakEstimate()
	// {
	// File f;
	// peakEstimate=new double[71];
	// BufferedReader bis = null;
	// try {
	// f = new File("Z:\\PWMNG\\peakEstimation3times.txt");
	// bis = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
	// } catch (IOException e) {
	// System.out.println("Uh oh, got an IOException error!" + e.getMessage());
	// } finally {
	// }
	// try {
	// String line = bis.readLine();
	// int i=0;
	// while(line!=null)
	// {
	// String[] numbers= new String[1];
	// numbers = line.trim().split(" ");
	// peakEstimate[i++] = Double.parseDouble(numbers[0]);
	// //System.out.println("Readed inputTime= " + inputTime + " Job Reqested
	// Time=" + j.startTime+" Total job so far="+ total);
	// line = bis.readLine();
	// }
	// } catch (IOException ex) {
	// System.out.println("readJOB EXC readJOB false ");
	// Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
	// }
	// }
	/*
	 * void coordinator(int times) { //return every server in ready state then
	 * try to make some of them idle for(int
	 * j=0;j<webSet1.ComputeNodeList.size();j++) {
	 * webSet1.ComputeNodeList.get(j).ready=1;
	 * webSet1.ComputeNodeList.get(j).currentCPU=0;
	 * webSet1.ComputeNodeList.get(j).Mips=1; }
	 * ///////////////////////////////////////////////////////////////////////
	 * int suc=0; if(times>=70) return; double peak=peakEstimate[times]; int
	 * numberOfidleServer=webSet1.ComputeNodeList.size()-(int)Math.ceil(peak/
	 * 1000); //System.out.println(numberOfidleServer); if(numberOfidleServer<0)
	 * return; for(int j=0;j<numberOfidleServer;j++)
	 * if(webSet1.ComputeNodeList.get(j).queueLength==0) { suc++;
	 * webSet1.ComputeNodeList.get(j).ready=-1;
	 * webSet1.ComputeNodeList.get(j).currentCPU=0;
	 * webSet1.ComputeNodeList.get(j).Mips=1; } else {System.out.println(
	 * "In Coordinator and else   ");numberOfidleServer++;} //border ra jabeja
	 * mikonim //if(suc==numberOfidleServer)
	 * System.out.println(numberOfidleServer+"\t suc= "+suc); } public void
	 * addToresponseArray(double num,int time) { responseTime t= new
	 * responseTime(); t.numberOfJob=num; t.responseTime=time;
	 * responseArray.add(t); return; }
	 */
}
