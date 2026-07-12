package Obj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.StringTokenizer;

public class ObjIO {

	public static NProjObj loadProjObj(File f) {
		NProjObj proj = readProjObj(f);
		if (proj == null) {
			proj = new NProjObj(ProjObj.loadProjObj(f));

			File dir = new File(f.getParent());
			proj.setWorkingPath(dir.getAbsolutePath());
			File df = new File(f.getParent(), "project_data");
			proj.setDataFile(df.getAbsolutePath());
			proj.setSimVector();
			writeProjFile(proj);
		}

		return proj;

	}

	public static NSimObj loadSimObj(NProjObj proj, String simName) {

		NSimObj sim = readSimObj(proj, simName);

		return sim;
	}

	private static NProjObj readProjObj(File f) {

		String projName = "";
		boolean useIndLabel = false;
		boolean usePopId = false;
		boolean usePopFlag = false;
		boolean useGeneName = false;
		boolean useLocusName = false;
		boolean usePhaseInfo = false;
		boolean usePhenoType = false;
		boolean oneRow = false;
		boolean phased = false;
		boolean recessiveAllele = false;
		boolean locData = false;

		int extraCol = 0;
		int numInds = 0;
		int ploidy = 2;
		int numloci = 0;
		String missingVal = "-9";
		String notambiguous = "-939";
		@SuppressWarnings("unused")
		String dataFile;

		// load data to memory
		StringBuffer buffer = new StringBuffer();
		String content = "";
		try {
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis, "UTF8");

			Reader in = new BufferedReader(isr);
			int ch;
			while ((ch = in.read()) > -1) {
				buffer.append((char) ch);
			}
			in.close();
			content = buffer.toString();
		} catch (IOException e) {
			return null;
		}

		if (content.indexOf("PROJNAME") == -1) {
			return null;
		}

		// parse the data file
		String str = new String();
		StringTokenizer file_st = new StringTokenizer(content, "\n");
		while (file_st.hasMoreTokens()) {

			str = file_st.nextToken();
			StringTokenizer st = new StringTokenizer(str);
			String key = st.nextToken();
			String val = st.nextToken();

			if (key.equals("PROJNAME")) {
				projName = val;
			}

			// general info

			if (key.equals("NUMINDS")) {
				try {
					numInds = Integer.parseInt(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("NUMLOCI")) {
				try {
					numloci = Integer.parseInt(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("PLOIDY")) {
				try {
					ploidy = Integer.parseInt(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("MISSINGVAL")) {
				missingVal = val;
			}

			if (key.equals("ONEROW")) {
				try {
					int bv = Integer.parseInt(val);
					if (bv == 1) {
						oneRow = true;
					}

				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("INDLABEL")) {
				try {
					int bv = Integer.parseInt(val);
					if (bv == 1) {
						useIndLabel = true;
					}

				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("POPID")) {
				try {
					int bv = Integer.parseInt(val);
					if (bv == 1) {
						usePopId = true;
					}

				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("LOCDATA")) {
				try {
					int bv = Integer.parseInt(val);
					if (bv == 1) {
						locData = true;
					}

				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("POPFLAG")) {
				try {
					int bv = Integer.parseInt(val);
					if (bv == 1) {
						usePopFlag = true;
					}

				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("PHENOTYPE")) {
				try {
					int bv = Integer.parseInt(val);
					if (bv == 1) {
						usePhenoType = true;
					}

				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("EXTRACOL")) {
				try {
					extraCol = Integer.parseInt(val);
				} catch (Exception e) {
					return null;
				}
			}

			// rows
			if (key.equals("GENENAME") || key.equals("MARKERNAME")) {
				try {
					int bv = Integer.parseInt(val);
					if (bv == 1) {
						useGeneName = true;
					}

				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("RECESSIVEALLELE")) {
				try {
					int bv = Integer.parseInt(val);
					if (bv == 1) {
						recessiveAllele = true;
					}

				} catch (Exception e) {
					return null;
				}
			}

			// set when ploidy > 3 and recessive alleles model specified
			if (key.equals("NOTAMBIGUOUS")) {
				notambiguous = val;
			}

			if (key.equals("LOCUSNAME") || key.equals("MAPDISTANCE")) {
				try {
					int bv = Integer.parseInt(val);
					if (bv == 1) {
						useLocusName = true;
					}

				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("PHASED")) {
				try {
					int bv = Integer.parseInt(val);
					if (bv == 1) {
						phased = true;
					}

				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("PHASEINFO")) {
				try {
					int bv = Integer.parseInt(val);
					if (bv == 1) {
						usePhaseInfo = true;
					}

				} catch (Exception e) {
					return null;
				}
			}

		}

		NProjObj proj = new NProjObj(projName, null, useIndLabel, usePopId,
				extraCol, numInds, ploidy, numloci, missingVal, null,
				usePopFlag, useGeneName, useLocusName, usePhaseInfo, oneRow,
				phased, usePhenoType, recessiveAllele, locData);

		if (ploidy >= 3 && recessiveAllele) {
			proj.setNAMBCode(notambiguous);
		}

		File dir = new File(f.getParent());
		proj.setWorkingPath(dir.getAbsolutePath());
		File df = new File(f.getParent(), "project_data");
		proj.setDataFile(df.getAbsolutePath());
		proj.setSimVector();

		return proj;

	}

	private static NSimObj readSimObj(NProjObj proj, String simName) {
		if (proj == null) {
			return null;
		}

		NSimObj sim = new NSimObj(proj);

		boolean confirm = false;

		File simDir = new File(proj.getWorkingPath(), simName);
		File simFile = new File(simDir.getAbsolutePath(), ".sim");

		// reset sim path everytime open the sim.

		sim.setSimPath(simDir.getAbsolutePath());
		sim.setSimName(simName);

		// load data to memory
		StringBuffer buffer = new StringBuffer();
		String content = "";
		try {
			FileInputStream fis = new FileInputStream(simFile);
			InputStreamReader isr = new InputStreamReader(fis, "UTF8");

			Reader in = new BufferedReader(isr);
			int ch;
			while ((ch = in.read()) > -1) {
				buffer.append((char) ch);
			}
			in.close();
			content = buffer.toString();
		} catch (IOException e) {
			return null;
		}

		// parse the data file
		String str = new String();
		StringTokenizer file_st = new StringTokenizer(content, "\n");
		while (file_st.hasMoreTokens()) {

			str = file_st.nextToken();
			StringTokenizer st = new StringTokenizer(str);

			String key = st.nextToken();
			String val = st.nextToken();

			// run parameter

			if (key.equals("BURNIN")) {
				try {
					sim.BURNIN = Integer.parseInt(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("NUMREPS")) {
				try {
					sim.NUMREPS = Integer.parseInt(val);
					confirm = true;
				} catch (Exception e) {
					return null;
				}
			}

			// model params

			if (key.equals("NOADMIX")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.NOADMIX = false;
					} else {
						sim.NOADMIX = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("LOCPRIOR")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 1) {
						sim.LOCPRIOR = true;
					} else {
						sim.LOCPRIOR = false;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("RECOMBINE") || key.equals("LINKAGE")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.RECOMBINE = false;
					} else {
						sim.RECOMBINE = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("USEPOPINFO")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.USEPOPINFO = false;
					} else {
						sim.USEPOPINFO = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("FREQSCORR")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.FREQSCORR = false;
					} else {
						sim.FREQSCORR = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("ONEFST")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.ONEFST = false;
					} else {
						sim.ONEFST = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("INFERALPHA")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.INFERALPHA = false;
					} else {
						sim.INFERALPHA = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("POPALPHAS")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.POPALPHAS = false;
					} else {
						sim.POPALPHAS = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("ALPHA")) {
				try {
					sim.ALPHA = Float.parseFloat(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("INFERLAMBDA")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.INFERLAMBDA = false;
					} else {
						sim.INFERLAMBDA = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("POPSPECIFICLAMBDA")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.POPSPECIFICLAMBDA = false;
					} else {
						sim.POPSPECIFICLAMBDA = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("LAMBDA")) {
				try {
					sim.LAMBDA = Float.parseFloat(val);
				} catch (Exception e) {
					return null;
				}
			}

			// leftover from proj

			if (key.equals("MARKOVPHASE")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.MARKOVPHASE = false;
					} else {
						sim.MARKOVPHASE = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			// priors

			if (key.equals("FPRIORMEAN")) {
				try {
					sim.FPRIORMEAN = Float.parseFloat(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("FPRIORSD")) {
				try {
					sim.FPRIORSD = Float.parseFloat(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("UNIFPRIORALPHA")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.UNIFPRIORALPHA = false;
					} else {
						sim.UNIFPRIORALPHA = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("ALPHAMAX")) {
				try {
					sim.ALPHAMAX = Float.parseFloat(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("ALPHAPRIORA")) {
				try {
					sim.ALPHAPRIORA = Float.parseFloat(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("ALPHAPRIORB")) {
				try {
					sim.ALPHAPRIORB = Float.parseFloat(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("RMIN")) {
				try {
					sim.RMIN = Float.parseFloat(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("RMAX")) {
				try {
					sim.RMAX = Float.parseFloat(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("RSTD")) {
				try {
					sim.RSTD = Float.parseFloat(val);
				} catch (Exception e) {
					return null;
				}
			}
			if (key.equals("RSTART")) {
				try {
					sim.RSTART = Float.parseFloat(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("RSTEP")) {
				try {
					sim.RSTEP = Float.parseFloat(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("GENSBACK")) {
				try {
					sim.GENSBACK = Integer.parseInt(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("MIGRPRIOR")) {
				try {
					sim.MIGRPRIOR = Float.parseFloat(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("PFROMPOPFLAGONLY")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.PFROMPOPFLAGONLY = false;
					} else {
						sim.PFROMPOPFLAGONLY = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			// sub model for popinfo

			if (key.equals("POPNOADMIX")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.POPNOADMIX = false;
					} else {
						sim.POPNOADMIX = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("POPRECOMBINE")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.POPRECOMBINE = false;
					} else {
						sim.POPRECOMBINE = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			// output option

			if (key.equals("PRINTQ")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.PRINTQ = false;
					} else {
						sim.PRINTQ = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("SITEBYSITE")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.SITEBYSITE = false;
					} else {
						sim.SITEBYSITE = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			// miscellaneous

			if (key.equals("COMPUTEPROB")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.COMPUTEPROB = false;
					} else {
						sim.COMPUTEPROB = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("ADMBURNIN")) {
				try {
					sim.ADMBURNIN = Integer.parseInt(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("USEDEFAULTADMBURNIN")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.USEDEFAULTADMBURNIN = false;
					} else {
						sim.USEDEFAULTADMBURNIN = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("ALPHAPROPSD")) {
				try {
					sim.ALPHAPROPSD = Float.parseFloat(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("STARTPOPINFO")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.STARTPOPINFO = false;
					} else {
						sim.STARTPOPINFO = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("METROFREQ")) {
				try {
					sim.METROFREQ = Integer.parseInt(val);
				} catch (Exception e) {
					return null;
				}
			}

			// /////////////////////////////////////////////////////////////////////////////////////////////

			// not shown
			if (key.equals("ANCESTDIST")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.ANCESTDIST = false;
					} else {
						sim.ANCESTDIST = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("NUMBOXES")) {
				try {
					sim.NUMBOXES = Integer.parseInt(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("ANCESTPINT")) {
				try {
					sim.ANCESTPINT = Float.parseFloat(val);
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("FQSETLAMBDA")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.FQSETLAMBDA = false;
					} else {
						sim.FQSETLAMBDA = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("FQINFERLAMBDA")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.FQINFERLAMBDA = false;
					} else {
						sim.FQINFERLAMBDA = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("INDIFQSETLAMBDA")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.INDIFQSETLAMBDA = false;
					} else {
						sim.INDIFQSETLAMBDA = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

			if (key.equals("INDIFQINFERLAMBDA")) {
				try {
					int temp = Integer.parseInt(val);
					if (temp == 0) {
						sim.INDIFQINFERLAMBDA = false;
					} else {
						sim.INDIFQINFERLAMBDA = true;
					}
				} catch (Exception e) {
					return null;
				}
			}

		}
		sim.isNew = false;
		if (confirm) {
			return sim;
		}
		return null;
	}

	public static void writeProjFile(NProjObj proj) {
		File f = new File(proj.workingPath, proj.projName + ".spj");
		PrintStream out = OpenOutputFile(f);

		out.println("PROJNAME" + "  " + proj.projName);

		// general

		out.println("NUMINDS" + "  " + proj.numInds);
		out.println("NUMLOCI" + "  " + proj.numloci);
		out.println("PLOIDY" + "  " + proj.ploidy);
		out.println("MISSINGVAL" + "  " + proj.missingVal);

		if (proj.oneRow == true) {
			out.println("ONEROW" + "   1");
		} else {
			out.println("ONEROW" + "   0");
		}

		// columns

		if (proj.useIndLabel == true) {
			out.println("INDLABEL" + "   1");
		} else {
			out.println("INDLABEL" + "   0");
		}

		if (proj.usePopId == true) {
			out.println("POPID" + "   1");
		} else {
			out.println("POPID" + "   0");
		}

		if (proj.usePopFlag == true) {
			out.println("POPFLAG" + "   1");
		} else {
			out.println("POPFLAG" + "   0");
		}

		if (proj.locData == true) {
			out.println("LOCDATA" + "   1");
		} else {
			out.println("LOCDATA" + "   0");
		}

		if (proj.usePhenoType == true) {
			out.println("PHENOTYPE" + "   1");
		} else {
			out.println("PHENOTYPE" + "   0");
		}

		out.println("EXTRACOL" + "  " + proj.extraCol);

		// rows

		if (proj.useGeneName == true) {
			out.println("MARKERNAME" + "   1");
		} else {
			out.println("MARKERNAME" + "   0");
		}

		if (proj.recessiveAllele == true) {
			out.println("RECESSIVEALLELE" + "   1");
			if (proj.ploidy >= 3) {
				out.println("NOTAMBIGUOUS" + "  " + proj.notambiguous);
			}
		} else {
			out.println("RECESSIVEALLELE" + "   0");
		}

		if (proj.useLocusName == true) {
			out.println("MAPDISTANCE" + "   1");
		} else {
			out.println("MAPDISTANCE" + "   0");
		}

		// additional row info
		if (proj.phased == true) {
			out.println("PHASED" + "   1");
		} else {
			out.println("PHASED" + "   0");
		}

		if (proj.usePhaseInfo == true) {
			out.println("PHASEINFO" + "   1");
		} else {
			out.println("PHASEINFO" + "   0");
		}

		out.close();
	}

	public static void writeSimFile(NSimObj sim) {
		File f = new File(sim.getSimPath(), ".sim");
		PrintStream out = OpenOutputFile(f);
		out.println("BURNIN" + "  " + sim.BURNIN);
		out.println("NUMREPS" + "  " + sim.NUMREPS);
		if (sim.FREQSCORR == true) {
			out.println("FREQSCORR" + "   1");
		} else {
			out.println("FREQSCORR" + "   0");
		}
		if (sim.ONEFST == true) {
			out.println("ONEFST" + "   1");
		} else {
			out.println("ONEFST" + "   0");
		}
		out.println("FPRIORMEAN" + "  " + sim.FPRIORMEAN);
		out.println("FPRIORSD" + "  " + sim.FPRIORSD);
		if (sim.FQSETLAMBDA == true) {
			out.println("FQSETLAMBDA" + "   1");
		} else {
			out.println("FQSETLAMBDA" + "   0");
		}
		if (sim.FQINFERLAMBDA == true) {
			out.println("FQINFERLAMBDA" + "   1");
		} else {
			out.println("FQINFERLAMBDA" + "   0");
		}
		if (sim.INDIFQSETLAMBDA == true) {
			out.println("INDIFQSETLAMBDA" + "   1");
		} else {
			out.println("INDIFQSETLAMBDA" + "   0");
		}
		if (sim.INDIFQINFERLAMBDA == true) {
			out.println("INDIFQINFERLAMBDA" + "   1");
		} else {
			out.println("INDIFQINFERLAMBDA" + "   0");
		}
		if (sim.NOADMIX == true) {
			out.println("NOADMIX" + "   1");
		} else {
			out.println("NOADMIX" + "   0");
		}
		if (sim.LOCPRIOR == true) {
			out.println("LOCPRIOR" + "   1");
		} else {
			out.println("LOCPRIOR" + "   0");
		}
		if (sim.USEDEFAULTADMBURNIN == true) {
			out.println("USEDEFAULTADMBURNIN" + "   1");
		} else {
			out.println("USEDEFAULTADMBURNIN" + "   0");
		}
		out.println("ADMBURNIN" + "  " + sim.ADMBURNIN);
		if (sim.INFERALPHA == true) {
			out.println("INFERALPHA" + "   1");
		} else {
			out.println("INFERALPHA" + "   0");
		}
		out.println("ALPHA" + "  " + sim.ALPHA);
		if (sim.POPALPHAS == true) {
			out.println("POPALPHAS" + "   1");
		} else {
			out.println("POPALPHAS" + "   0");
		}
		if (sim.UNIFPRIORALPHA == true) {
			out.println("UNIFPRIORALPHA" + "   1");
		} else {
			out.println("UNIFPRIORALPHA" + "   0");
		}
		out.println("ALPHAMAX" + "  " + sim.ALPHAMAX);
		out.println("ALPHAPROPSD" + "  " + sim.ALPHAPROPSD);
		out.println("ALPHAPRIORA" + "  " + sim.ALPHAPRIORA);
		out.println("ALPHAPRIORB" + "  " + sim.ALPHAPRIORB);
		if (sim.RECOMBINE == true) {
			out.println("RECOMBINE" + "   1");
		} else {
			out.println("RECOMBINE" + "   0");
		}
		if (sim.SITEBYSITE == true) {
			out.println("SITEBYSITE" + "   1");
		} else {
			out.println("SITEBYSITE" + "   0");
		}
		out.println("RSTART" + "  " + sim.RSTART);
		out.println("RSTEP" + "  " + sim.RSTEP);
		out.println("RMAX" + "  " + sim.RMAX);
		out.println("RMIN" + "  " + sim.RMIN);
		out.println("RSTD" + "  " + sim.RSTD);
		if (sim.MARKOVPHASE == true) {
			out.println("MARKOVPHASE" + "   1");
		} else {
			out.println("MARKOVPHASE" + "   0");
		}
		if (sim.USEPOPINFO == true) {
			out.println("USEPOPINFO" + "   1");
		} else {
			out.println("USEPOPINFO" + "   0");
		}
		out.println("GENSBACK" + "  " + sim.GENSBACK);
		out.println("MIGRPRIOR" + "  " + sim.MIGRPRIOR);
		if (sim.POPNOADMIX == true) {
			out.println("POPNOADMIX" + "   1");
		} else {
			out.println("POPNOADMIX" + "   0");
		}
		if (sim.POPRECOMBINE == true) {
			out.println("POPRECOMBINE" + "   1");
		} else {
			out.println("POPRECOMBINE" + "   0");
		}
		if (sim.INFERLAMBDA == true) {
			out.println("INFERLAMBDA" + "   1");
		} else {
			out.println("INFERLAMBDA" + "   0");
		}
		if (sim.POPSPECIFICLAMBDA == true) {
			out.println("POPSPECIFICLAMBDA" + "   1");
		} else {
			out.println("POPSPECIFICLAMBDA" + "   0");
		}
		out.println("LAMBDA" + "  " + sim.LAMBDA);
		if (sim.COMPUTEPROB == true) {
			out.println("COMPUTEPROB" + "   1");
		} else {
			out.println("COMPUTEPROB" + "   0");
		}
		if (sim.ANCESTDIST == true) {
			out.println("ANCESTDIST" + "   1");
		} else {
			out.println("ANCESTDIST" + "   0");
		}
		if (sim.PRINTQ == true) {
			out.println("PRINTQ" + "   1");
		} else {
			out.println("PRINTQ" + "   0");
		}
		out.println("NUMBOXES" + "  " + sim.NUMBOXES);
		out.println("ANCESTPINT" + "  " + sim.ANCESTPINT);
		if (sim.STARTPOPINFO == true) {
			out.println("STARTPOPINFO" + "   1");
		} else {
			out.println("STARTPOPINFO" + "   0");
		}
		if (sim.PFROMPOPFLAGONLY == true) {
			out.println("PFROMPOPFLAGONLY" + "   1");
		} else {
			out.println("PFROMPOPFLAGONLY" + "   0");
		}
		out.println("METROFREQ" + "  " + sim.METROFREQ);
		out.close();
	}

	private static PrintStream OpenOutputFile(File file) {

		PrintStream out;
		try { // try to open output file as well...
			out = new PrintStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			System.err.println("**Error: unable to open output file.");
			out = null;
		}// try-catch
		catch (SecurityException e) {
			System.err.println("**Error: no permission to write output file.");
			out = null;
		}// try-catch
		
		return out;
	}

}
