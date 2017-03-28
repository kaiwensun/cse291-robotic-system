#include "mbed.h"
#include "Hexi_OLED_SSD1351.h"
#include <stdio.h>
#include <math.h>


int main() {  
    /* Instantiate the SSD1351 OLED Driver */ 
     /* (MOSI,SCLK,POWER,CS,RST,DC) */
    SSD1351 oled(p5,p7,p13,p10,p9,p8);
    
    /* Get OLED Class Default Text Properties */
    oled_text_properties_t textProperties = {0};
    oled.GetTextProperties(&textProperties);    
    
    /* Fills the screen with solid black */         
    oled.FillScreen(COLOR_BLACK);
        
    /* Change font color to blue */ 
    textProperties.fontColor   = COLOR_BLUE;
    oled.SetTextProperties(&textProperties);
    
        
    /* Set text properties to white and right aligned for the dynamic text */
    textProperties.fontColor = COLOR_YELLOW;
    textProperties.alignParam = OLED_TEXT_ALIGN_RIGHT;
    oled.SetTextProperties(&textProperties); 
    
    oled.radar(48, 48, 20, COLOR_YELLOW);
}

