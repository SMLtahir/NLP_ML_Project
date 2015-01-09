/**
 * 
 */
package pythagoras.wekaProcessors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * 
 * @author Tahir Sousa
 * @version last updated: Nov 8, 2014 [Sousa]
 */
public class PrepareAblativeRun {

	//List of Main Group IDs
	//"TB_", "LS_", "Ph_&&Phon_", "Disc_", "SE_", "CSur_", "CSyn_", "CNg_"
	//Use && to separate 2 feature codes if both are to be excluded in the same run
	//Refer to the group codes as given on PythagorasJavaExperiment.java main file
	private static final String[] FEATURE_CODES = {"CSur_&&TB_", "CSur_&&LS_", "CSur_&&Ph_&&Phon_", "CSur_&&Disc_", "CSur_&&SE_", "CSur_&&CSyn_", "CSur_&&CNg_"};		
	
	public static void main(String[] args) throws IOException {

		String inputFilepath = "E:\\Eclipse\\DKPro_Workspace\\Pythagoras\\de.tudarmstadt.ukp.dkpro.lab\\repository\\";
		String inputFileName = "Classification_gem-training-data";
//		String inputFileName = "overall-training-data";
		for(int codeId=0; codeId< FEATURE_CODES.length; codeId++)	{
			String unneededFeatureCode = FEATURE_CODES[codeId];
			
			String OUTPUT_FILEPATH = inputFilepath + inputFileName + unneededFeatureCode + ".arff";
			
			List<String> originalDataLines = FileUtils.readLines(new File(inputFilepath + inputFileName+ ".arff"));
			List<Integer> removedAttributeIds = new ArrayList<Integer>(); 
			String outputString = "";
			
			int lineNr = 0;
			System.out.println("Total lines in original file: "+ originalDataLines.size());
			
			for(; lineNr< originalDataLines.size(); lineNr++)	{
				String line = originalDataLines.get(lineNr);
				
				if(line.contains("@attribute"))
					break;		//Go to attribute definitions processor
				
				outputString += line+ "\n";
				System.out.print(line+ "\n");
			}
			
			int attributeCount = -1;
			int finalAttributes = 0;
			//Go through the attribute definitions
			for(; lineNr< originalDataLines.size(); lineNr++)	{
				String line = originalDataLines.get(lineNr);
				if(!line.equals("") && line.contains("@attribute "))
					attributeCount++;
				boolean containsUnneededFeatCode = false;
				
				//If unneededFeatureCode is a combination of more than one main Group Ids
				if(unneededFeatureCode.contains("&&"))	{
					String[] codes = unneededFeatureCode.split("&&");
					for(String code : codes)	{
						if(line.contains("@attribute "+ code))	{
							removedAttributeIds.add(attributeCount);
							containsUnneededFeatCode = true;
							break;
						}
					}	
				}
				
				//If unneededFeatureCode is a single main group id
				if(line.contains("@attribute "+ unneededFeatureCode))	{
					removedAttributeIds.add(attributeCount);
					containsUnneededFeatCode = true;
				}
				
				if(containsUnneededFeatCode == false)	{
					outputString += line+ "\n";
					if(!line.equals("") && line.contains("@attribute "))
						finalAttributes++;
					System.out.print(line+ "\n");
				}
				
				if(line.contains("@data"))	{
					lineNr++;
					break;		//Go to feature values processor
				}
					
			}
			
			
			//Go through feature values
			for(; lineNr< originalDataLines.size(); lineNr++)	{
				String line = originalDataLines.get(lineNr);
				String featureValuesLine = "";
				if(!line.equals(""))	{
					String[] splitLine = line.split(",");
					for(int i=0; i< splitLine.length; i++)	{
						if(!removedAttributeIds.contains(i))	{
							featureValuesLine += splitLine[i]+",";
						}
					}
					outputString += featureValuesLine.substring(0, featureValuesLine.length()-1);		//Trim the final comma ","
					System.out.print(featureValuesLine.substring(0, featureValuesLine.length()-1));
				}
				outputString += "\n";
				System.out.print("\n");
			}

			System.out.println("Total number of attributes originally: "+ (attributeCount+1));
			System.out.println("Total number of attributes finally: "+ finalAttributes);
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(OUTPUT_FILEPATH)));
			writer.write(outputString);
			writer.close();
		
		}
		
		WekaMainProcessor.alertUserWhenDone();
	}

}
