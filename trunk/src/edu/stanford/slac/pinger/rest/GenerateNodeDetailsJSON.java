package edu.stanford.slac.pinger.rest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import edu.stanford.slac.pinger.general.C;

public class GenerateNodeDetailsJSON {


	private static void generateNodesFile(boolean all) {
		if (all) {
			String url = "http://www-iepm.slac.stanford.edu/pinger/pingerworld/all-nodes.cf";
			String htmlContent = HttpGetter.readPage(url);
			htmlContent = htmlContent.replaceAll("\"(.*)\"(.*)\"(.*)\"", "\"$1'$2'$3\"");
			C.writeIntoFile(htmlContent, "data/perl/all-nodes.cf");
		} else {
			String url = "http://www-iepm.slac.stanford.edu/pinger/pingerworld/nodes.cf";
			String htmlContent = HttpGetter.readPage(url);
			C.writeIntoFile(htmlContent, "data/perl/nodes.cf");
		}
	}

	/**
	 * This function uses a perl script to generate the JSON for NodeDetails.
	 * It first tries to use the environment variable 'perl' to run the script. If the variable is not set, it tries to use the PERL_HOME set in the class C.java.
	 * @param generateNodesFile
	 * @param all
	 */
	public static void start(boolean generateNodesFile, boolean all) {
		if (generateNodesFile)
			generateNodesFile(all);
		try {
			boolean withPerlHome = false;
			boolean success = false;
			String line = null;
			Process proc = null;
			BufferedReader input = null;

			try {
				String cmd = "perl -e \"print 'Testing'\"";
				proc = Runtime.getRuntime().exec(cmd);
				input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				line = input.readLine();				
				success = line.equals("Testing");
			} catch (Exception e) {
				System.out.println(e);
				System.out.println("Could not execute a perl test. Environment variable 'perl' is not set properly.");
				System.out.println("Now trying with the PERL_HOME set in the project...");
				String cmd = "\""+C.PERL_HOME+"\\perl\" -e \"print 'Testing'\"";
				proc = Runtime.getRuntime().exec(cmd);
				input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				line = input.readLine();
				input.close();
				proc.destroy();
				if (line.equals("Testing")) {
					withPerlHome = true;
					success = true;
				}
			} finally {
				input.close();
				proc.destroy();
			}
			if (success)
				generate(withPerlHome);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void generate(boolean withPerlHome) {
		try {
			PrintWriter out = new PrintWriter(C.JSON_NODES_FILE);
			generatePerlScript();
			String cmd = null;
			if (withPerlHome)
				cmd = "\""+C.PERL_HOME+"\\perl\" \""+C.PROJECT_HOME+"\\data\\perl\\getNodeDetails.pl\"";
			else
				cmd = "perl \""+C.PROJECT_HOME+"\\data\\perl\\getNodeDetails.pl\"";
			System.out.println(cmd);
			Process proc = Runtime.getRuntime().exec(cmd);
			BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
				out.println(line);
			}
			input.close();
			out.close();
			System.out.println("JSON NODE_DETAILS generated!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void generatePerlScript() {
		try {
			String filePath = "data/perl/getNodeDetails.pl";

			BufferedReader br = new BufferedReader(new FileReader(filePath));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			String content = sb.toString();
			br.close();
			String rplc = C.PROJECT_HOME.replace("\\", "/");
			content = content.replaceAll("my [$]home = '.*';", "my \\$home = '"+rplc+"';");
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, false)));
			out.print(content);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main (String args[]) {
		start(false, false);
	}


}
