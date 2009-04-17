//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.host;

import java.util.regex.Pattern;

import jd.PluginWrapper;
import jd.http.Browser;
import jd.http.Encoding;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.LinkStatus;
import jd.plugins.Plugin;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

public class ImageFap extends PluginForHost {

    public ImageFap(PluginWrapper wrapper) {
        super(wrapper);
        this.setStartIntervall(500l);

        Browser.setRequestIntervalLimitGlobal(getHost(), 200);
    }

    private String DecryptLink(String code) {
        try {
            String s1 = Encoding.htmlDecode(code.substring(0, code.length() - 1));

            String t = "";
            for (int i = 0; i < s1.length(); i++) {
                // logger.info("decrypt4 " + i);
                // logger.info("decrypt5 " + ((int) (s1.charAt(i+1) - '0')));
                // logger.info("decrypt6 " +
                //(Integer.parseInt(code.substring(code.length()-1,code.length()
                // ))));
                int charcode = s1.charAt(i) - Integer.parseInt(code.substring(code.length() - 1, code.length()));
                // logger.info("decrypt7 " + charcode);
                t = t + new Character((char) charcode).toString();
                // t+=new Character((char)
                // (s1.charAt(i)-code.charAt(code.length()-1)));

            }
            // logger.info(t);
            // var s1=unescape(s.substr(0,s.length-1)); var t='';
            //for(i=0;i<s1.length;i++)t+=String.fromCharCode(s1.charCodeAt(i)-s.
            // substr(s.length-1,1));
            // return unescape(t);
            // logger.info("return of DecryptLink(): " +
            // JDUtilities.htmlDecode(t));
            return Encoding.htmlDecode(t);
        } catch (Exception e) {
            jd.controlling.JDLogger.getLogger().log(java.util.logging.Level.SEVERE,"Exception occured",e);
        }
        return null;
    }

    @Override
    public String getAGBLink() {
        return "http://imagefap.com/faq.php";
    }

    @Override
    public boolean getFileInformation(DownloadLink downloadLink) {
        try {
            br.getPage(downloadLink.getDownloadURL());
            String picture_name = new Regex(br, Pattern.compile("<td bgcolor='#FCFFE0' width=\"100\">Filename</td>.*?<td bgcolor='#FCFFE0'>(.*?)</td>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)).getMatch(0);
            String gallery_name = new Regex(br, Pattern.compile("<a href=\"gallery\\.php\\?gid=\\d+\"><font face=verdana size=3>(.*?)uploaded", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)).getMatch(0);
            String uploader_name = new Regex(br, Pattern.compile("<a href=\"/profile\\.php\\?user=(.*?)\" style=\"text-decoration: none;\"", Pattern.CASE_INSENSITIVE)).getMatch(0);
            if (gallery_name != null) {
                gallery_name = gallery_name.trim();
            }
            if (picture_name != null) {
                FilePackage fp = FilePackage.getInstance();
                fp.setName(uploader_name);
                if (gallery_name != null) {
                    downloadLink.setName(gallery_name + " + " + picture_name);
                } else {
                    downloadLink.setName(picture_name);
                }
                downloadLink.setFilePackage(fp);
                return true;
            }
        } catch (Exception e) {
            jd.controlling.JDLogger.getLogger().log(java.util.logging.Level.SEVERE,"Exception occured",e);
        }
        return false;
    }

    @Override
    public String getVersion() {
        return getVersion("$Revision$");
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
        br.setFollowRedirects(true);
        br.getPage(downloadLink.getDownloadURL());
        br.setDebug(true);
        String picture_name = new Regex(br, Pattern.compile("<td bgcolor='#FCFFE0' width=\"100\">Filename</td>.*?<td bgcolor='#FCFFE0'>(.*?)</td>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)).getMatch(0);
        if (picture_name == null) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String gallery_name = new Regex(br, Pattern.compile("<a href=\"gallery\\.php\\?gid=\\d+\"><font face=verdana size=3>(.*?)uploaded", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)).getMatch(0);
        if (gallery_name != null) {
            gallery_name = gallery_name.trim();
        }

        /* DownloadLink holen */
        String imagelink = DecryptLink(new Regex(br, Pattern.compile("return lD\\('(\\S+?)'\\);", Pattern.CASE_INSENSITIVE)).getMatch(0));

        String filename = Plugin.extractFileNameFromURL(imagelink);
        downloadLink.setFinalFileName(filename);
        if (gallery_name != null) downloadLink.addSubdirectory(gallery_name);
        dl = br.openDownload(downloadLink, imagelink);
        if (dl.getConnection().getResponseCode() == 404) {
            dl.getConnection().disconnect();
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        dl.startDownload();
    }

    public int getMaxSimultanFreeDownloadNum() {
        return 10;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetPluginGlobals() {
    }
}
