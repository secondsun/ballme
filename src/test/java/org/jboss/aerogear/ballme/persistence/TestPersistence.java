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
package org.jboss.aerogear.ballme.persistence;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import org.jboss.aerogear.ballme.vo.Game;
import org.jboss.aerogear.ballme.vo.Invitation;
import org.jboss.aerogear.ballme.vo.Party;
import org.jboss.aerogear.ballme.vo.Person;
import org.jboss.aerogear.ballme.vo.Response;
import org.jboss.aerogear.ballme.vo.Team;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestPersistence {
    
    private static final byte[] TINY_GIF = new byte[] { (byte) 0x47b,(byte) 0x49b,(byte) 0x46b,(byte) 0x38b,(byte) 0x39b,(byte) 0x61b,(byte) 0x0Ab,(byte) 0x00b,(byte) 0x0Ab,(byte) 0x00b,(byte) 0x91b,(byte) 0x00b,(byte) 0x00b,(byte) 0xFFb,(byte) 0xFFb,(byte) 0xFFb,(byte) 0xFFb,(byte) 0x00b,(byte) 0x00b,(byte) 0x00b,(byte) 0x00b,(byte) 0xFFb,(byte) 0x00b,(byte) 0x00b,(byte) 0x00b,(byte) 0x21b,(byte) 0xF9b,(byte) 0x04b,(byte) 0x00b,(byte) 0x00b,(byte) 0x00b,(byte) 0x00b,(byte) 0x00b,(byte) 0x2Cb,(byte) 0x00b,(byte) 0x00b,(byte) 0x00b,(byte) 0x00b,(byte) 0x0Ab,(byte) 0x00b,(byte) 0x0Ab,(byte) 0x00b,(byte) 0x00b,(byte) 0x02b,(byte) 0x16b,(byte) 0x8Cb,(byte) 0x2Db,(byte) 0x99b,(byte) 0x87b,(byte) 0x2Ab,(byte) 0x1Cb,(byte) 0xDCb,(byte) 0x33b,(byte) 0xA0b,(byte) 0x02b,(byte) 0x75b,(byte) 0xECb,(byte) 0x95b,(byte) 0xFAb,(byte) 0xA8b,(byte) 0xDEb,(byte) 0x60b,(byte) 0x8Cb,(byte) 0x04b,(byte) 0x91b,(byte) 0x4Cb,(byte) 0x01b,(byte) 0x00b,(byte) 0x3Bb };
    
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "ballme.war")
                .addPackages(true, Person.class.getPackage())
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource("wildfly-ds.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private UserTransaction utx;
 
    @Before
    public void startTransaction() throws Exception {
        utx.begin();
        em.joinTransaction();
    }
    
    @After
    public void rollBack() throws Exception {
        utx.rollback();
    }
    
    @Test
    public void testTeamCRUD() {
        Team t = createTeam(0);
        em.persist(t);
        em.flush();
        
        Assert.assertNotNull(t.getId());
        Assert.assertNotNull(t.getVersion());
        
        Team storedTeam = em.find(Team.class, t.getId());
        Assert.assertEquals(t.getColor(), storedTeam.getColor());
        Assert.assertArrayEquals(t.getPicture(), storedTeam.getPicture());
        Assert.assertEquals(t.getName(), storedTeam.getName());
        Assert.assertEquals(t.getId(), storedTeam.getId());
        Assert.assertEquals(t.getVersion(), storedTeam.getVersion());
        
        em.remove(t);
        em.flush();
        
        Team nullTeam = em.find(Team.class, t.getId());
        Assert.assertNull(nullTeam);
    }
    
    @Test 
    public void testGameCrud() {
        Team homeTeam = createNewPersistedTeam(1);
        Team awayTeam = createNewPersistedTeam(2);
        
        Game game = createGame(homeTeam, awayTeam, 3);
        em.persist(game);
        em.flush();
        
        Long gameVersion = game.getVersion();
        
        Assert.assertNotNull(game.getId());
        Assert.assertNotNull(gameVersion);
        
        Game savedGame = em.find(Game.class, game.getId());
        
        Assert.assertEquals(homeTeam, savedGame.getHomeTeam());
        Assert.assertEquals(awayTeam, savedGame.getAwayTeam());
        Assert.assertEquals(game.getAwayScore(), savedGame.getAwayScore());
        Assert.assertEquals(game.getHomeScore(), savedGame.getHomeScore());
        Assert.assertEquals(game.getStartTime(), savedGame.getStartTime());
        Assert.assertEquals(game.getEndTime(), savedGame.getEndTime());
        
        
        
        savedGame.setAwayScore(42);
        em.merge(savedGame);
        em.flush();
        savedGame = em.find(Game.class, game.getId());

        Assert.assertNotEquals(gameVersion, savedGame.getVersion());
        
        em.remove(game);
        Game nullGame = em.find(Game.class, game.getId());
        Assert.assertNull(nullGame);
        
    }
    
    @Test
    public void testPersonCrud() {
        Person p = createPersistedPerson(1);
        
        Assert.assertNotNull(p.getId());
        Assert.assertNotNull(p.getVersion());
        
        Person searchPerson = em.find(Person.class, p.getId());
        
        Assert.assertArrayEquals(TINY_GIF, searchPerson.getPicture());
        Assert.assertEquals(p.getFavoriteTeam(), searchPerson.getFavoriteTeam());
        Assert.assertEquals(p.getName(), searchPerson.getName());
        
        em.remove(p);
        
        Person nullPerson = em.find(Person.class, p.getId());
        
        Assert.assertNull(nullPerson);
        
    }
    
    @Test
    public void testPartyCrud() {
        Party p = createPersistedParty(1);
        
        Party searchParty = em.find(Party.class, p.getId());
        
        Assert.assertEquals(p.getHost(), searchParty.getHost());
        Assert.assertEquals(p.getGame(), searchParty.getGame());
        Assert.assertEquals(p.getGuests(), searchParty.getGuests());
        Assert.assertEquals(5, searchParty.getGuests().size());
        Assert.assertEquals(p.getLongitude(), searchParty.getLongitude());
        Assert.assertEquals(p.getLatitude(), searchParty.getLatitude());
        
        
    }
    
    
    @Test
    public void testInvitationCrud() {
        Invitation i = new Invitation();
        i.setEvent(createPersistedParty(5));
        i.setRecipient(createPersistedPerson(7));
        i.setResponse(Response.NO);
        
        em.persist(i);
        em.flush();
        em.detach(i);
        Assert.assertNotNull(i.getVersion());
        
        Invitation foundInvitation = em.find(Invitation.class, i.getId());
        Assert.assertEquals(i.getResponse(), foundInvitation.getResponse());
        
        foundInvitation.setResponse(Response.YES);
        
        em.merge(foundInvitation);
        em.flush();
        
        Assert.assertNotEquals(i.getVersion(), foundInvitation.getVersion());
        
        em.remove(foundInvitation);
        
        Assert.assertNull(em.find(Invitation.class, i.getId()));
        
    }
    
    private Team createTeam(int number) {
        Team t = new Team();
        t.setColor(number);
        t.setPicture(TINY_GIF);
        t.setName("Test Team" + number);
        return t;
    }
    
    private Game createGame(Team home, Team away, int number) {
        Game g = new Game();
        g.setHomeTeam(home);
        g.setAwayTeam(away);
        g.setAwayScore(number * 2);
        g.setHomeScore(number * 3);
        g.setStartTime(today(number));
        g.setEndTime(today(number + 2));
        return g;
    }

    private Date today(int hourOfDay) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private Game createNewPersistedGame(Team homeTeam, Team awayTeam, int number) {
        Game g = createGame(homeTeam, awayTeam, number);
        em.persist(g);
        em.flush();
        return g;
    }
    
    private Team createNewPersistedTeam(int number) {
        Team t = createTeam(number);
        em.persist(t);
        em.flush();
        return t;
    }

    private Person createPerson(int number) {
        Person p = new Person();
        p.setPicture(TINY_GIF);
        p.setName("Test Person " + number);
        p.setFavoriteTeam(createNewPersistedTeam(number * 41));
        return p;
    }
    
    private Person createPersistedPerson(int number) {
        Person p = createPerson(number);
        em.persist(p);
        em.flush();
        return p;
    }

    private Party createPersistedParty(final int number) {
        Party p = new Party();
        p.setStartTime(today(number));
        p.setEndTime(today(5 + number));
        p.setGuests(new HashSet<Person>(){{
            add(createPersistedPerson(12 * number));
            add(createPersistedPerson(14 * number));
            add(createPersistedPerson(15 * number));
            add(createPersistedPerson(16 * number));
            add(createPersistedPerson(17 * number));
            
        }});
        p.setGame(createNewPersistedGame(createNewPersistedTeam(1 * number), createNewPersistedTeam(2 * number), 3 * number));
        p.setHost(createPersistedPerson(4 * number));
        p.setLatitude(BigDecimal.valueOf(38 * number));
        p.setLongitude(BigDecimal.valueOf(42 * number));
        p.setName("Game");
        p.setPicture(TINY_GIF);
        
        em.persist(p);
        em.flush();
        return p;
    }
    
}

