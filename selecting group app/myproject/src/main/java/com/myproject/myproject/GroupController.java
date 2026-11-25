package com.myproject.myproject;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class GroupController {

    @PostMapping("/group")
    public ResponseEntity<String> group(@RequestParam String names, @RequestParam int count, HttpSession session) {
        if (session.getAttribute("username") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in.");
        }

        // 1. Hatiin ang text
        String[] raw = names.split(",");

        // 2. Bilangin ang mga hindi blangko
        int total = 0;
        for (int i = 0; i < raw.length; i++) {
            if (!raw[i].trim().isEmpty()) {
                total = total + 1;
            }
        }

        // 3. Ilagay sa bagong array ang mga hindi blangko lang
        String[] people = new String[total];
        int index = 0;
        for (int i = 0; i < raw.length; i++) {
            if (!raw[i].trim().isEmpty()) {
                people[index] = raw[i].trim();
                index = index + 1;
            }
        }

        // 4. Check kung tama ang bilang ng grupo
        if (count < 1 || count > total) {
            return ResponseEntity.badRequest().body(
                    "Wrong number of groups. Please enter a number between 1 and " + total);
        }

        // 5. I-shuffle
        Random random = new Random();
        for (int i = 0; i < total; i++) {
            int j = random.nextInt(total);
            String temp = people[i];
            people[i] = people[j];
            people[j] = temp;
        }

        // 6. Gumawa ng resulta
        String result = "";
        for (int g = 0; g < count; g++) {
            result = result + "Group " + (g + 1) + ": ";
            for (int i = g; i < total; i = i + count) {
                result = result + people[i] + ", ";
            }
            result = result + "\n";
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/pick")
    public ResponseEntity<String> pick(@RequestParam String names, HttpSession session) {
        if (session.getAttribute("username") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in.");
        }
        String[] people = names.split(",");
        int total = 0;
        for (String person : people) {
            if (!person.trim().isEmpty()) {
                total++;
            }
        }
        if (total == 0) {
            return ResponseEntity.badRequest().body("Please enter some names.");
        }

        String[] validPeople = new String[total];
        int index = 0;
        for (String person : people) {
            if (!person.trim().isEmpty()) {
                validPeople[index] = person.trim();
                index++;
            }
        }

        Random random = new Random();
        int pickedIndex = random.nextInt(validPeople.length);
        return ResponseEntity.ok(validPeople[pickedIndex]);
    }
}