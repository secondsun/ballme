/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and
 * individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jboss.aerogear.ballme.tasks;

import android.support.v7.graphics.Palette;
import com.elasticstats.feed.schema.ncaafb.hierarchy_v1_0.ConferenceType;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import com.elasticstats.feed.schema.ncaafb.hierarchy_v1_0.DivisionType;
import com.elasticstats.feed.schema.ncaafb.hierarchy_v1_0.SubdivisionType;
import com.elasticstats.feed.schema.ncaafb.hierarchy_v1_0.TeamType;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.persistence.PersistenceContextType;
import net.saga.googleimagegetter.Getter;
import org.jboss.aerogear.ballme.vo.Team;

@ApplicationScoped
@Singleton
@Startup()
public class FetchTeamInfo {
    
    private static Logger LOG = Logger.getLogger(FetchTeamInfo.class.getSimpleName());
    
    @PersistenceContext
    private EntityManager em;
    
    
    @PostConstruct
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void checkTeamInfo() {
        List teamList = em.createQuery("from Team t").getResultList();
        if (teamList.isEmpty()) {
            refreshInfo();
        }
    }
    
    @Schedule(dayOfWeek="Sun", hour="0")
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void refreshInfo() {
        
        try {            
            String url = "https://api.sportsdatallc.org/ncaafb-t1/teams/FBS/hierarchy.xml?api_key="+System.getProperty("sportsdata.api.key");
            
            JAXBContext context = JAXBContext.newInstance("com.elasticstats.feed.schema.ncaafb.hierarchy_v1_0");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement divisionElement = (JAXBElement) unmarshaller
					.unmarshal(new URL(url).openStream());
            
            DivisionType division =  (DivisionType) divisionElement.getValue();
            for (ConferenceType conference : division.getConference()) {
                for (SubdivisionType subdivision : conference.getSubdivision()) {
                    for (TeamType team : subdivision.getTeam()) {
                        LOG.log(Level.INFO, team.getMarket() + " " + team.getName());
                        Team t = new Team();
                        t.setName(team.getMarket() + " " + team.getName());
                        byte[] imageData = Getter.getImage(t.getName(), System.getProperty("cse.key"), System.getProperty("cse.id"));
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
                        Palette p = Palette.generate(image, 4);
                    
                        t.setColor(p.getVibrantColor(0x000));
                        t.setPicture(imageData);
                        em.persist(t);
                    }
                }
            }
            em.flush();
            
            
        } catch (Exception ex) {
            Logger.getLogger(FetchTeamInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
         
    }
    
}
