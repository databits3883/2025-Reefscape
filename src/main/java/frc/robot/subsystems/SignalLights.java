// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.LEDPattern;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.LEDConstants;
import frc.robot.subsystems.CoolArm.ArmAction;

public class SignalLights extends SubsystemBase {
  
  public AddressableLED LEDs;
  public AddressableLEDBuffer LEDBuffer = new AddressableLEDBuffer(0);

  public boolean hasAlgae = false;
  public boolean hadAlgae = true;


  public ArmAction currentArmAction = ArmAction.Travel;
  
  public ArmAction previousArmAction = ArmAction.Pickup;

  public boolean inClimbMode = false;

  public boolean autoAligned = false;
  
  public boolean wasAutoAligned = true;

  private LightSignal storedSignal = LightSignal.Idle;


  public LightSignal currentSignal = LightSignal.databits;

  //These are used to know when we toggle or change the LED state.
  private LightSignal previousSignal= LightSignal.hasAlgae;


  public enum LightSignal {
    hasAlgae,
    scoringMode,
    loadMode,
    climbPrep,
    climbFinish,
    databits,
    climbTime,
    Idle
  }

  /** Creates a new SignalLights. */
  public SignalLights() {
    LEDs = new AddressableLED(LEDConstants.LED_PORT);


    LEDBuffer = new AddressableLEDBuffer(LEDConstants.LED_COUNT);
    //SetArmLEDBufferToSolidColor(LEDConstants.kDatabitsColor);
    LEDs.setLength(LEDConstants.LED_COUNT);
    //Shuffleboard.getTab("Debug 2").addDouble("Driverstation Match Time", DriverStation.);
    //armLEDs.setData(armLEDBuffer);
    //armLEDs.start();
    currentSignal = LightSignal.Idle;
  }

  @Override
  public void periodic() {
    boolean ledChanged = true;

    double matchTime = DriverStation.getMatchTime();
    if(matchTime < 17 && matchTime > 15){
      //System.out.println(matchTime);
      if(currentSignal != LightSignal.climbTime){
        storedSignal = currentSignal;
      }
      currentSignal = LightSignal.climbTime;
    }
    else if (currentSignal == LightSignal.climbTime){
      currentSignal = storedSignal;
    }

    //Determine if we push LED update
    if (previousSignal != currentSignal) 
    {
      previousSignal = currentSignal;
      ledChanged = true;

      if(currentSignal != LightSignal.climbFinish && currentSignal != LightSignal.Idle){
        inClimbMode = false;
      }
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
      case scoringMode:
        if(previousArmAction != currentArmAction || wasAutoAligned != autoAligned){
          
          if(autoAligned){
            System.out.println("aligned and lights know it");
            if(currentArmAction == ArmAction.L4){
              SetLEDPattern(LEDConstants.kScoreL4_aligned);
            }
            else if (currentArmAction == ArmAction.L3){
              SetLEDPattern(LEDConstants.kScoreL3_aligned);
            }
            else if (currentArmAction == ArmAction.L2){
              SetLEDPattern(LEDConstants.kScoreL2_aligned);
            }
            else if (currentArmAction == ArmAction.L1){
              SetLEDPattern(LEDConstants.kScoreL1_aligned);
            }
          }
          else{
            if(currentArmAction == ArmAction.L4){
              SetLEDPattern(LEDConstants.kScoreL4_notAligned);
            }
            else if (currentArmAction == ArmAction.L3){
              SetLEDPattern(LEDConstants.kScoreL3_notAligned);
            }
            else if (currentArmAction == ArmAction.L2){
              SetLEDPattern(LEDConstants.kScoreL2_notAligned);
            }
            else if (currentArmAction == ArmAction.L1){
              SetLEDPattern(LEDConstants.kScoreL1_notAligned);
            }
          }

          
          ledChanged = true;
        }
        
        break;
      case loadMode:
        SetLEDPattern(LEDConstants.kLoadModeColor);
        break;
      case climbPrep:      
        SetLEDPattern(LEDConstants.kClimbReadyColor);
        break;
      case climbTime:      
        SetLEDPattern(LEDConstants.kClimbTimeBlink);
        
        break;
      case climbFinish:
        SetLEDPattern(LEDConstants.kClimbFinishColor);
        ledChanged = true;
        inClimbMode = true;
        break;

      case Idle:
        if(!inClimbMode){
          if(DriverStation.isDSAttached() || DriverStation.isFMSAttached()){
            SetLEDPattern(LEDConstants.kAnimatedIdle);
          }
          else{
            SetLEDPattern(LEDConstants.kAnimatedIdle_Disconnected);
          }
        }
        else{
          SetLEDPattern(LEDConstants.kClimbFinishColor);
          inClimbMode = true;
        }
        
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
    wasAutoAligned = autoAligned;
    previousArmAction = currentArmAction;

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

  public void DisablePartyMode(){
    inClimbMode = false;
  }


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

  public void ReceiveArmAction(ArmAction action){
    currentArmAction = action;
  }

  public void SetSignal(LightSignal signal){
    currentSignal = signal;
  }

  
}