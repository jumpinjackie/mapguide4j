package util;

import play.*;

import java.util.*;
import java.io.*;

//Verbatim impl of MgLocalizer

public class MgLocalizationUtil
{
    protected static String english = "en";
    protected static String localizationPath = "";
    protected static Hashtable languages = new Hashtable();

    public static void reset()
    {
        languages.clear();
    }

    public static void SetLocalizedFilesPath(String path)
    {
        localizationPath = path;
    }

    public static String Localize(String text, String locale, int os)
    {
        String fontSuffix = (os==0? "Windows": (os==1? "Macintosh": "Linux"));
        Hashtable sb = null;
        try
        {
            sb = GetStringBundle(locale);
        }
        catch (Exception e)
        {
            return "";
        }
        int len = text.length();

        for (int i = 0; i < len; )
        {
            int pos1 = text.indexOf("__#", i);
            if (pos1 != -1)
            {
                int pos2 = text.indexOf("#__", pos1 + 3);
                if (pos2 != -1)
                {
                    String id = text.substring(pos1 + 3, pos2);
                    String locStr;
                    locStr = (String)sb.get(id.equals("@font") || id.equals("@fontsize") ? id + fontSuffix : id);
                    if (locStr == null)
                        locStr = "";
                    int locLen = locStr.length();

                    String begin, end;
                    if (pos1 > 0)
                        begin = text.substring(0, pos1);
                    else
                        begin = "";
                    end = text.substring(pos2 + 3);
                    text = begin + locStr + end;

                    len = len - 6 - id.length() + locLen;
                    i = pos1 + locLen;
                }
                else
                    i = len;
            }
            else
                i = len;
        }
        return text;
    }

    public static String GetString(String id, String locale)
    {
        Hashtable sb = null;
        try
        {
            sb = GetStringBundle(locale);
        }
        catch (Exception e)
        {
            return "";
        }
        String s = (String)sb.get(id);
        if (s == null)
            return "";
        return s;
    }

    protected static Hashtable GetStringBundle(String locale)
    {
        if (locale.equals(""))
            locale = english;
        else
            locale = locale.toLowerCase();

        if (!languages.containsKey(locale))
        {
            Logger.debug("Loading in string bundle for: " + locale);
            BufferedReader in = null;
            try
            {
                File f = new File(localizationPath + locale);
                if (!f.exists())
                {
                    // requested locale is not supported, default to English
                    if (languages.containsKey(english))
                        return (Hashtable)languages.get(english);
                    f = new File(localizationPath + english);
                }
                Logger.debug("Loading in string bundle: " + f.getPath());
                in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
                String line;
                Hashtable sb = new Hashtable();
                while ((line = in.readLine()) != null)
                {
                    line = line.trim();
                    if (line.equals("") || line.charAt(0) == '#')
                        continue;
                    int sep = line.indexOf('=');
                    if (sep == -1)
                        continue;
                    String key = line.substring(0, sep).trim();
                    if (key.equals(""))
                        continue;
                    sb.put(key, line.substring(sep + 1).trim());
                }
                languages.put(locale, sb);
                Logger.debug("Loaded string bundle: " + locale);
            }
            catch (Exception e)
            {
                //I need apache commons just for a class to get the full exception details to string? GTFO!
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                Logger.debug(sw.toString());
                return null;
            }
            finally
            {
                if (in != null)
                {
                    try { in.close(); }
                    catch (Exception e) { }
                }
            }
        }
        return (Hashtable)languages.get(locale);
    }
}