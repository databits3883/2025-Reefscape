// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.LEDPattern;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.LEDConstants;

public class SignalLights extends SubsystemBase {
  
  public AddressableLED LEDs;
  public AddressableLEDBuffer LEDBuffer = new AddressableLEDBuffer(LEDConstants.LED_COUNT);

  public boolean hasAlgae = false;
  public boolean hadAlgae = true;


  public LightSignal currentSignal = LightSignal.databits;

  //These are used to know when we toggle or change the LED state.
  private LightSignal previousSignal= LightSignal.hasAlgae;


  public enum LightSignal {
    hasAlgae,
    rightAlign,
    leftAlign,
    climbPrep,
    climbFinish,
    databits,
    databitsAnimated
  }

  /** Creates a new SignalLights. */
  public SignalLights() {
    LEDs = new AddressableLED(LEDConstants.LED_PORT);


    LEDBuffer = new AddressableLEDBuffer(LEDConstants.LED_COUNT);
    //SetArmLEDBufferToSolidColor(LEDConstants.kDatabitsColor);
    LEDs.setLength(LEDConstants.LED_COUNT);
    //armLEDs.setData(armLEDBuffer);
    //armLEDs.start();
    currentSignal = LightSignal.databitsAnimated;
  }

  @Override
  public void periodic() {
    boolean ledChanged = false;
    //Determine if we push LED update
    if (previousSignal != currentSignal) 
    {
      previousSignal = currentSignal;
      ledChanged = true;

    }
    // This method will be called once per scheduler run
    switch (currentSignal) {

      case hasAlgae:
        if(hadAlgae != hasAlgae){
          if(hasAlgae){
            SetLEDPattern(LEDConstants.kYesAlgaeColor);
          }
          else{
            SetLEDPattern(LEDConstants.kNoAlgaeColor);
          }
        }
          
        break;
      case leftAlign:
        SetLEDPattern(LEDConstants.kAlignColor);
        break;
      case rightAlign:
        SetLEDPattern(LEDConstants.kAlignColor);
        break;
      case climbPrep:      
        SetLEDPattern(LEDConstants.kClimbReadyColor);
        break;
      case climbFinish:
        SetLEDPattern(LEDConstants.kClimbFinishColor);
        ledChanged = true;
        break;

      case databitsAnimated:
        SetLEDPattern(LEDConstants.kDatabitsAnimated);
        //This needs to update every loop
        ledChanged = true;
        break;
    
      default:
      SetLEDPattern(LEDConstants.kErrorColor);
        ledChanged = true;
        break;

      
    }

    //Only push the LED state if it has changed
    if (ledChanged)
    {
      LEDs.setData(LEDBuffer);
      LEDs.start(); 
      
    
    }
    hadAlgae = hasAlgae;
  }

  private void SetLEDPattern(LEDPattern pattern){
    pattern.applyTo(LEDBuffer);
  }

  // private void WaveColorWithTime(Color color, double timer) {
  //   animationCounter +=animationStepSize;
  //   if(animationCounter>180){
  //     animationCounter = 0;
  //   }
  //   //double timerDeg = Units.degreesToRadians(timer);
  //   for (int i = 0; i < leftLEDBuffer.getLength(); i++) {
      
  //     double brightness  = ((Math.sin( Units.degreesToRadians( i*animationStepSize + animationCounter ) ) + 1) / 2) / 8;
  //     leftLEDBuffer.setRGB(i, (int)(brightness * color.red), (int)(brightness * color.green), (int)(brightness * color.blue));
  //   }

  //   for (int i = 0; i < rightLEDBuffer.getLength(); i++) {
      
  //     double brightness  = ((Math.sin( Units.degreesToRadians( i*animationStepSize + animationCounter ) ) + 1) / 2) / 8;
  //     rightLEDBuffer.setRGB(i, (int)(brightness * color.red), (int)(brightness * color.green), (int)(brightness * color.blue));
  //   }
   
  // }


  // private void PartyMode(double timer) {
  //   animationCounter +=animationStepSize;
  //   if(animationCounter>180){
  //     animationCounter = 0;
  //   }
  //   //double timerDeg = Units.degreesToRadians(timer);
  //   for (int i = 0; i < leftLEDBuffer.getLength(); i++) {
      
  //     leftLEDBuffer.setHSV(i, (animationCounter + (i*animationStepSize))%180, 255, 255);
  //   }

  //   for (int i = 0; i < rightLEDBuffer.getLength(); i++) {
      
  //     rightLEDBuffer.setHSV(i, (animationCounter + (i*animationStepSize))%180, 255, 255);
  //   }
   
  // }

  // public void SetArmLEDBuffersToSolidColor(Color color){

  //   for (var i = 0; i < leftLEDBuffer.getLength(); i++) {
      
  //     leftLEDBuffer.setLED(i, color);
  //   }
   
  //   for (var i = 0; i < rightLEDBuffer.getLength(); i++) {
      
  //     rightLEDBuffer.setLED(i, color);
  //   }
  // }

  // public void SetOneSideLEDBuffersToSolidColor(Color color, boolean isRight){

  //   for (var i = 0; i < leftLEDBuffer.getLength(); i++) {
      
  //     leftLEDBuffer.setLED(i, isRight ? LEDConstants.kOffColor : color);
  //   }
   
  //   for (var i = 0; i < rightLEDBuffer.getLength(); i++) {
      
  //     rightLEDBuffer.setLED(i, isRight ? color : LEDConstants.kOffColor);
  //   }
  // }

  public void ReceiveIntakeData(boolean algae){
    hasAlgae = algae;
  }

  public void SetSignal(LightSignal signal){
    currentSignal = signal;
  }

  
}