package tech.mainak.kundali.kundali_2.controllers;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tech.mainak.kundali.kundali_2.dto.ContactRequest;
import tech.mainak.kundali.kundali_2.service.MessageService;

@Controller
@AllArgsConstructor
public class HomeController {

    private final MessageService messageService;
    private final Logger logger = LoggerFactory.getLogger(HomeController.class);
    @GetMapping("/")
    public String home() {
        return "Home";
    }

    @PostMapping("/contact")
    public ResponseEntity<String> Contact(@RequestBody ContactRequest contactRequest) {
        logger.warn("BirthChart API required: "+"\nName: "+contactRequest.getName()+"\nEmail: "+contactRequest.getEmail()+"\nPhone: "+contactRequest.getPhone()+"\nMessage: "+contactRequest.getMessage());
        String message="BirthChart API required: "+"\nname: "+contactRequest.getName()+"\nemail: "+contactRequest.getEmail()+"\nPhone: "+contactRequest.getPhone()+"\nmessage: "+contactRequest.getMessage();
        try{
            messageService.sendMessage(message);
            return ResponseEntity.ok("ok");
        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }
}
