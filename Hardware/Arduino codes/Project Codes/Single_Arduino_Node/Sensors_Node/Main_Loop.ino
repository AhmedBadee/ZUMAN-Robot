void loop() {
  Time_Now = millis();
  if( Time_Now - Last_Tic > Sample_Time){
    Last_Tic = Time_Now;
    
    if(Yaw_Goal > Yaw_Angle){
      Yaw_Angle ++;
    }else if(Yaw_Goal < Yaw_Angle){
      Yaw_Angle --;
    }
    if(Pitch_Goal > Pitch_Angle){
      Pitch_Angle ++;
    }else if(Pitch_Goal < Pitch_Angle){
      Pitch_Angle --;
    }
  
    Yaw.write(Yaw_Angle);
    Pitch.write(Pitch_Angle);
  
    if (Yaw_Goal == Yaw_Angle && Pitch_Goal == Pitch_Angle && Cam_Done){
      Cam_Done = LOW;
      hw_msg.arg1 = Yaw_Goal;
      hw_msg.arg2 = Pitch_Goal;
      info_Pub.publish(&hw_msg);
    }
  }
  nh.spinOnce();
   
}

