package com.dashjoin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.dashjoin.jsonata.Jsonata;

import static com.dashjoin.jsonata.Jsonata.jsonata;
import static java.util.Map.of;

public class Test
{
    public static void main(String[] args)
    {
        System.out.println("Hello world!");
        Jsonata expr1 = jsonata("$transform($, {\"name\": $cert.verifiedDomains[0].type })");
        
        Map<String, Object> input = new HashMap<>();
        input.put("verifiedDomains", List.of(of(
                "capabilities", "Email, OfficeCommunicationsOnline",
                "isDefault", true,
                "isInitial", true,
                "name", "y5l4g.onmicrosoft.com",
                "type", "Managed"
        )));
        var res1 = expr1.evaluate(input);
        Jsonata.Frame bindingFrame = new Jsonata.Frame(null);
        
        
        bindingFrame.bind("cert", input);
        expr1.evaluate(input,bindingFrame);
        System.out.println(res1);
        
    }
}