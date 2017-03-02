//Program to calculate percent likelihood of traffic occurring on the 5 freeway

import java.util.*;
import java.io.*;
import java.lang.Math;

public class CarSim2 {
	//constants/globals to record average car length, percentage of self-driving cars, car density per hour, and finalized data
	static double carLen = 14.7;
	static double percent_self_driving;
	static double[] car_density = {.01, .01, .01, .01, .01, .03, .08, .06, .05, .05, .05, .05, .05, .06, .06, .07, .08, .07, .04, .04, .03, .03, .03, .02};
	static double[] running;

	public static void main(String[] args) throws FileNotFoundException {
		//PrintWriter to write to our destination file
		PrintWriter p = new PrintWriter(new File("results1.txt"));

		//variables to hold data from given spreadsheet
		int ID;
		double start;
		double end;
		int avg_cars;
		int dec_lanes;
		int inc_lanes;
		String type;
		String line;

		//for loop that cycles through amount of self-driving cars on the road
		for(percent_self_driving = 0; percent_self_driving < 1.01; percent_self_driving = percent_self_driving + .05)
		{
			//creates a new final data storage array for each amount of self-driving cars and initializes it to zero
			//last two indices represent primary and secondary saved space
			running = new double[26];
			for(int i = 0; i < 26; i++)
			{
				running[i] = 0;
			}

			//scanner to read from .txt containing excel info
			Scanner read = new Scanner(new File("dataMCM1.txt"));
			while(read.hasNextLine())
			{
				//process and parse data
				line = read.nextLine();
				String[] s = line.split(",");
				ID = Integer.parseInt(s[0]);
				start = Double.valueOf(s[1]);
				end = Double.valueOf(s[2]);
				avg_cars = Integer.parseInt(s[3]);
				type = s[4];
				dec_lanes = Integer.parseInt(s[5]);
				inc_lanes = Integer.parseInt(s[6]);
				double distance = (end - start) * 5280;
				int total_lanes = dec_lanes + inc_lanes;

				
				double[] raw_milage_available = Chance_Calculations(avg_cars, car_density, total_lanes, distance);
				for(int j = 0; j < 26; j++)
				{
					running[j] += raw_milage_available[j];
				}
				
			}
			
			//for loop to convert raw milage to a percentage based on min and max amount of space available
			for(int j = 0; j < 26; j++)
			{
				if(j < 24)
					running[j] = -100 * (((running[j] / 135) - 8000) / 659000);
			}

			//print iterations worth of data to text doc
			p.printf("%.2f,", percent_self_driving);
			for(int k = 0; k < 26; k++)
			{
				if(k == 25)
					p.printf("%.2f\n", running[k]);
				else
					p.printf("%.2f,", running[k]);
			}
			read.close();
		}
		p.close();
	}

	//function that calculates raw milage available on roads
	public static double[] Chance_Calculations(int avg_cars, double car_density[], int total_lanes, double distance){
		//calculate area available on section of highway
		double road_area = distance * total_lanes;
		double[] cars_at_hour = new double[26];

		for (int i= 0; i < car_density.length; i++) {
			//calculate amount of total cars are on the highway at a given hour
			cars_at_hour[i] = avg_cars * car_density[i];

			//calculate amount of self-driving and regular cars on the road
			double total_self_driving = cars_at_hour[i] * percent_self_driving;
			double num_norm = cars_at_hour[i] - total_self_driving;

			//calculate the extra space based on road size, number of cars, and average car size
			double communication = SpaceChange(total_self_driving);
			cars_at_hour[i] = road_area - (num_norm * 54.7) - (total_self_driving * 32.7) + communication;

			//record saved space for analysis
	 		running[24] = running[24] + (22 * total_self_driving);
			running[25] = running[25] + communication;
		}
		return cars_at_hour;
	}
	
	//calculates additional space saved if two self-driving cars are next to one another
	public static double SpaceChange(double total_self_driving) {
		//if cars are adjacent the following distance becomes a constant 1 foot
		double percentage = Math.pow(percent_self_driving, 2);
		percentage *= total_self_driving;
		percentage = percentage *17;
		return percentage;
	
	}

}