/**
 * © Copyright IBM Corporation 2015, 2017.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

/**
 * Purpose: The goal of this class is to minimize the repeating String manipulation
 * code written when readining in parameters. The methods here are meant to be
 * highly repeatable and easy to use. All methods assume they are being given a
 * string with the end goal of returning an array to iterate through.
 * Note: Assumed that all lists are separated by commas and/or newlines.
 * All whitespace is eliminated.
 */
package com.urbancode.air.plugin.informatica;
public class TextBoxParser {

    // Standardizes file name and confirms existance
    public boolean doesFileExist(String path) {
        def exitVal = false
        File f = new File(path)
        // Check if the given path exists
        if (f.isFile()) {
            exitVal = true
        }
        return exitVal
    }

    // Standardizes file name, confirms existance, and returns file text if exists
    public String retrieveFileText(String file) {
        def path = file.replace('\\', '/')
        File f = new File(path)
        // Check if the given path exists
        if (f.isFile()) {
            file =  f.getText('UTF-8')
        }
        return file
    }

    // Reads info, checks if input is a file, gets file text if it is
    // Otherwise, parses string as is into a String[]
    // Use Case: Enable user to enter either filename or string list as input for a
    // textAreaBox.
    public String[] readFileorTextString(String input) {
        def output = []
        f = retrieveFileText(input)
        output = text2StringArray(input)
        return output
    }

    // Reads info, checks if input is a file, gets file text if it is
    // Otherwise, parses string as is into a Long[]
    // Use Case: Enable user to enter either filename or Long list as input for a
    // textAreaBox.
    public Long[] readFileorTextLong(String input) {
        def output = []
        f = retrieveFileText(input)
        output = text2LongArray(input)
        return output
    }

    // Takes a string, separates it based on commas and new lines
    // Then places each new element into a String array
    // Can choose to remove all spaces or just trim()
    // Use Case: Parse names, IP Addresses, and when any character can be entered
    // Only removes beginning and ending whitespace
    public String [] text2StringArray(String input) {
        return text2StringArray(input, false)
    }

    // Can specify to remove all whitespace
    public String[] text2StringArray(String input, boolean removeAllSpaces) {
        def output
        if (input) {
            output = input.split('\n').join(',').split(',').findAll{
                it && it.trim().size() > 0} as String[]
            if (removeAllSpaces) {
                output = output.collect {it.replaceAll("\\s","")}
            }
            else {
                output = output.collect {it.trim()}
            }
        }
        return output
    }

    // Takes a string, separates it based on commas and new lines
    // then places each new element into an array of IP Addresses
    // All extra /, https:, and http: are removed.
    public String[] text2IPAddressArray(String input) {
        def output
        if (input) {
            output = input.split('\n').join(',').split(',').findAll{
                it && it.trim().size() > 0} as String[]

            output = output.collect {it.replaceAll("https://","")}
            output = output.collect {it.replaceAll("http://","")}
            output = output.collect {it.replaceAll("/","")}
            output = output.collect {it.trim()}
        }
        return output
    }

    // Takes a string, separates it based on commas and new lines
    // Then places each new element into a Long array
    // Use Case: Parse ports and other integers (negative or positive)
    public Long[] text2LongArray(String input) {
        def output = []
        if (input) {
            def tempStringArray = []
            tempStringArray = input.split('\n').join(',').split(',').findAll{
                it && it.trim().size() > 0} as String[]
            tempStringArray.each {it.trim().replaceAll("\\s","")}

            for (temp in tempStringArray){
                try {
                    output.add(Long.parseLong(temp.replaceAll("\\s","")))
                } catch (Exception ex) {
                    throw new Exception("You entered an invalid character in an Integer only field. Please ensure integers and commas are the only characters entered.")
                }
            }
        }
        return output
    }
}
