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
    
    wait(1);
    
    //part I - print book
    oled.printf("Escape characters test on printf():\n\r\
1.Test on \\r \
\r2.Test on \\n\n \
3.Test on %%d [%d]\n \
\r4.Test on %%04d [%04d]\n \
\r5.Test on %%f [%f]\n \
\r6.Test on %%+0.5f [%+0.5f]\n \
\r7.Test on %%c [%c] \
\r8.Test on %%s [%s]\n \
\r9.Test ends.",12,12,5.6,5.6,'A',"str");
    oled.printf("Test to print a book:\r\n");
    oled.printf("If you really want to hear about it, the first thing youll probably want to know is \
where I was born, and what my lousy childhood was like, and how my parents were \
occupied and all before they had me, and all that David Copperfield kind of crap, but I \
dont feel like going into it, if you want to know the truth. In the first place, that stuff \
bores me, and in the second place, my parents would have about two hemorrhages apiece \
if I told anything pretty personal about them. Theyre quite touchy about anything like \
that, especially my father. Theyre nice and all--Im not saying that--but theyre also \
touchy as hell. Besides, Im not going to tell you my whole goddam autobiography or \
anything. Ill just tell you about this madman stuff that happened to me around last \
Christmas just before I got pretty run-down and had to come out here and take it easy. I \
mean thats all I told D.B. about, and hes my brother and all. Hes in Hollywood. That \
isnt too far from this crumby place, and he comes over and visits me practically every \
week end. Hes going to drive me home when I go home next month maybe. He just got a \
Jaguar. One of those little English jobs that can do around two hundred miles an hour. It \
cost him damn near four thousand bucks. Hes got a lot of dough, now. He didnt use to. \
He used to be just a regular writer, when he was home. He wrote this terrific book of \
short stories, The Secret Goldfish, in case you never heard of him. The best one in it was \
The Secret Goldfish. It was about this little kid that wouldnt let anybody look at his \
goldfish because hed bought it with his own money. It killed me. Now hes out in \
Hollywood, D.B., being a prostitute. If theres one thing I hate, its the movies. Dont even \
mention them to me.");
    //part II - radar
    //oled.radar(48, 48, 20, COLOR_YELLOW);

    
}