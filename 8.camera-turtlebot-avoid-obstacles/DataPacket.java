class CoreSensorData{
	int time_stamp;
	int bumper;
	int wheel_drop;
	int cliff;
	int left_encoder;
	int right_encoder;
	int left_pwm;
	int right_pwm;
	int buttons;
	int charger;
	int battery;
	int over_current;
	
	public CoreSensorData(String str){
		String[] records = str.split(";");
		for(String record : records){
			String[] keyValue = record.split("=");
			if(keyValue.length==2){
				String key = keyValue[0];
				int value = Integer.parseInt(keyValue[1]);
				switch (key) {
				case "time_stamp":		time_stamp		= value; break;
				case "bumper":			bumper	 		= value; break;
				case "wheel_drop":		wheel_drop		= value; break;
				case "cliff":			cliff			= value; break;
				case "left_encoder":	left_encoder	= value; break;
				case "right_encoder":	right_encoder	= value; break;
				case "left_pwm":		left_pwm		= value; break;
				case "right_pwm":		right_pwm		= value; break;
				case "buttons":			buttons			= value; break;
				case "charger":			charger			= value; break;
				case "battery":			battery			= value; break;
				case "over_current":	over_current	= value; break;
				default:
					break;
				}
			}
		}
	}
	
	@Override
	public String toString(){
		String NL = System.getProperty("line.separator");
		String res = 	 "time_stamp	= "+	time_stamp	+";"+NL
				+"bumper        = "+	bumper	+";"+NL
				+"wheel_drop    = "+	wheel_drop	+";"+NL
				+"cliff         = "+	cliff	+";"+NL
				+"left_encoder  = "+	left_encoder	+";"+NL
				+"right_encoder = "+	right_encoder	+";"+NL
				+"left_pwm      = "+	left_pwm	+";"+NL
				+"right_pwm     = "+	right_pwm	+";"+NL
				+"buttons       = "+	buttons	+";"+NL
				+"charger       = "+	charger	+";"+NL
				+"battery       = "+	battery	+";"+NL
				+"over_current  = "+	over_current	+";";
		return res;
	}
}
