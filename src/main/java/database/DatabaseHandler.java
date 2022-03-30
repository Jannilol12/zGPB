package database;

import log.Logger;
import main.zGPB;
import timing.Event;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.*;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public class DatabaseHandler {

    private Connection connection;

    public void initiateDatabase() {
        try {
            connection = DriverManager.getConnection(
                    zGPB.INSTANCE.botConfigurationHandler.getConfigValue("zGPB_sql_database"),
                    zGPB.INSTANCE.botConfigurationHandler.getConfigValue("zGPB_sql_user"),
                    zGPB.INSTANCE.botConfigurationHandler.getConfigValue("zGPB_sql_password"));

            Logger.logDebugMessage("Initiated database connection");

            executeUpdateStatement("""
                    CREATE TABLE IF NOT EXISTS temporary_channels (
                    id INTEGER PRIMARY KEY,
                    owner BIGINT,
                    guild BIGINT,
                    name TEXT
                    )
                    """);

            executeUpdateStatement("""
                    CREATE TABLE IF NOT EXISTS reminders (
                    id INTEGER,
                    channel_id BIGINT,
                    message_id BIGINT,
                    user_id BIGINT,
                    time TEXT,
                    content TEXT
                    )
                    """);

            executeUpdateStatement("""
                    CREATE TABLE IF NOT EXISTS config (
                    guild_id BIGINT,
                    config_key VARCHAR(100),
                    config_value TEXT,
                    primary key (guild_id, config_key)
                    )
                    """);

            zGPB.INSTANCE.guildConfigurationHandler.loadOldConfig();
        } catch (SQLException throwables) {
            Logger.logException(throwables);
        }

    }

    public void insertConfig(long guild, String configKey, String configValue) {
        try (PreparedStatement ps = connection.prepareStatement("REPLACE INTO config VALUES (?,?,?)")) {
             ps.setLong(1, guild);
             ps.setString(2, configKey);
             ps.setString(3, configValue);
             ps.execute();
         } catch (SQLException e) {
             Logger.logException(e);
        }
    }

    public Map<Long, Map<String, String>> getConfig() {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM config")) {
            ResultSet rs = ps.executeQuery();

            Map<Long, Map<String, String>> out = new HashMap<>();

            while (rs.next()) {
                long guild = rs.getLong(1);
                if (!out.containsKey(guild))
                    out.put(guild, new HashMap<>());
                out.get(guild).put(rs.getString(2), rs.getString(3));
            }
            return out;
        } catch (SQLException e) {
            Logger.logException(e);
        }
        return null;
    }

    public void insertReminder(Event reminder) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO reminders VALUES (?,?,?,?,?,?)")) {
            ps.setLong(1, reminder.id());
            ps.setLong(2, reminder.channelID());
            ps.setLong(3, reminder.messageID());
            ps.setLong(4, reminder.userID());
            ps.setString(5, reminder.time().toString());
            ps.setString(6, reminder.content());
            ps.execute();
        } catch (SQLException e) {
            Logger.logException(e);
        }
    }

    public Set<Event> getReminders() {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM reminders")) {
            ResultSet rs = ps.executeQuery();
            Set<Event> out = new HashSet<>();
            while (rs.next())
                out.add(new Event(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getLong(4), ZonedDateTime.parse(rs.getString(5)), rs.getString(6)));
            return out;
        } catch (SQLException e) {
            Logger.logException(e);
        }
        return null;
    }

    public void removeReminder(Event e) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM reminders WHERE id = ?")) {
            ps.setLong(1, e.id());
            ps.execute();
        } catch (SQLException ex) {
            Logger.logException(ex);
        }
    }

    public void insertTemporaryChannel(long id, long owner, long guild, String name) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO temporary_channels VALUES (?,?,?,?)")) {
            ps.setLong(1, id);
            ps.setLong(2, owner);
            ps.setLong(3, guild);
            ps.setString(4, name);
            ps.execute();
        } catch (SQLException e) {
            Logger.logException(e);
        }
    }

    public Set<TemporaryChannel> getAllTemporaryChannels() {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM temporary_channels")) {
            ResultSet rs = ps.executeQuery();
            Set<TemporaryChannel> out = new HashSet<>();

            while (rs.next())
                out.add(new TemporaryChannel(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4)));

            return out;
        } catch (SQLException e) {
            Logger.logException(e);
        }
        return null;
    }

    public void removeTemporaryChannel(long id) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM temporary_channels WHERE id = ?")) {
            ps.setLong(1, id);
            ps.execute();
        } catch (SQLException e) {
            Logger.logException(e);
        }
    }

    public long getTemporaryChannelIDByNameAndAuthor(long author, String name) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT id FROM temporary_channels WHERE owner = ? AND name = ?")) {
            ps.setLong(1, author);
            ps.setString(2, name);
            ResultSet rs = ps.executeQuery();

            // SQLite Result Sets are closed if they are empty
            if (rs.isClosed())
                return -2;

            long firstId = rs.getLong(1);

            int c = 0;
            while (rs.next()) {
                c++;
            }

            // If there are more than 1 it is not ambiguous (for our purposes)
            if (c > 1)
                return -1;

            return firstId;
        } catch (SQLException e) {
            Logger.logException(e);
        }
        return -2;
    }

    public String getResultFromQuery(String sql) {
        StringBuilder sb = new StringBuilder();
        try {
            sql = sql.toLowerCase(Locale.ROOT);

            Statement query = connection.createStatement();

            ResultSet result = query.executeQuery(sql);
            ResultSetMetaData metaData = result.getMetaData();

            int columnCount = metaData.getColumnCount();

            sb.append(">").append(sql).append("<").append(System.lineSeparator());

            for (int i = 0; i < columnCount; i++)
                sb.append(metaData.getColumnName(i + 1)).append(", ");

            sb.replace(sb.length() - 2, sb.length(), "");

            sb.append(System.lineSeparator());

            while (result.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    sb.append(result.getString(i)).append(", ");
                }
                sb.replace(sb.length() - 2, sb.length(), "");
                sb.append(System.lineSeparator());
            }

            sb.replace(sb.length() - 1, sb.length(), "");

        } catch (SQLException e) {
            Logger.logException("internal sql error probably, " + sql);
            sb.setLength(0);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            sb.append(sw);
        }
        return sb.toString();
    }

    public void closeDatabaseConnection() {
        try {
            // Logger.logDebugMessage("Trying to backup memory database");
            // executeStatement("BACKUP TO data.db");
            connection.close();
        } catch (SQLException throwables) {
            Logger.logException(throwables);
        }
    }

    private void executeUpdateStatement(String sql) {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            Logger.logException(sql, e);
            System.err.println(sql);
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void executeStatement(String sql) {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            Logger.logException(sql, e);
        }
    }

}
