package dataman.utility.numsys.repository;

import dataman.dmbase.exception.DmException;
import dataman.dmbase.paging.dto.PagedResponse;
import dataman.dmbase.paging.dto.SearchRequest;
import dataman.dmbase.paging.service.DmPaging;
import dataman.dmbase.utils.DmUtil;
import dataman.utility.numsys.dto.*;
import dataman.utility.numsys.dto.IncludVocherTypeDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Repository
public class NumberSystemRepository {

    @Autowired
    @Qualifier("transactionNamedJdbcTemplate")
    private NamedParameterJdbcTemplate npjt;


    public PagedResponse<Map<String, Object>> getNavigator(SearchRequest searchRequest){

        String baseQuery = """
                    SELECT MAX(nsGroup) AS SearchCode FROM numbersystemformat  GROUP BY nsGroup
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();

        return DmPaging.getFilteredResult(npjt, baseQuery, searchRequest, params);
    }


    public PagedResponse<Map<String, Object>> fillType(String voucherType, SearchRequest searchRequest){

        String baseQuery = """
                    SELECT  V_Type,Description,short_Name FROM Voucher_Type  WHERE V_Type<> :voucherType
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("voucherType", voucherType);

        return DmPaging.getFilteredResult(npjt, baseQuery, searchRequest, params);
    }


    public PagedResponse<Map<String, Object>> getVoucherTypesExcluding(String vTypeCode, SearchRequest searchRequest) {
        String baseQuery = """
        SELECT VT.V_Type AS CODE,
               CONCAT(VT.Description, ' (', VT.short_Name, ')') AS NAME,
               VT.category,
               VT.Description
        FROM Voucher_Type VT
        WHERE VT.V_Type <> :vTypeCode
          AND VT.V_Type NOT IN (
              SELECT v_type FROM (
                  SELECT v_type FROM numberSystem
                  UNION ALL
                  SELECT nsGroup AS v_type FROM numbersystemformat
              ) AS tab
          )
        """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("vTypeCode", vTypeCode);

        return DmPaging.getFilteredResult(npjt, baseQuery, searchRequest, params);
    }

    public PagedResponse<Map<String, Object>> getAllVoucherTypes(SearchRequest searchRequest) {
        String baseQuery = """
                SELECT
                VT.V_Type AS CODE,
                CONCAT(VT.Description, ' (', VT.short_Name, ')') AS NAME,
                VT.category,
                VT.Description
                FROM
                Voucher_Type VT
                WHERE
                VT.v_type NOT IN (
                SELECT v_type FROM (
                SELECT v_type FROM numberSystem
                UNION ALL
                SELECT nsGroup AS v_type FROM numbersystemformat
                ) AS tab
                )
         """;

        MapSqlParameterSource params = new MapSqlParameterSource();


        return DmPaging.getFilteredResult(npjt, baseQuery, searchRequest, params);
    }

    public PagedResponse<Map<String, Object>> getCountList(String nsGroup, SearchRequest searchRequest){

        String baseQuery = """
                    SELECT nsc.nsFormatValue AS Value,
                    nsc.v_prefix  AS v_type,
                    nsc.site_Code AS scode,
                    sm.name AS snanm,
                    nsc.counter AS counter
                    FROM numberSystemCounter AS nsc\s
                    LEFT JOIN numberSystem AS ns ON nsc.nsGroup=ns.nsGroup
                    LEFT JOIN siteMast  AS sm ON sm.code = nsc.site_Code
                    LEFT JOIN voucher_Type AS vt ON vt.v_Type = nsc.nsGroup
                    WHERE nsc.nsGroup = :nsGroup
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("nsGroup", nsGroup);

        return DmPaging.getFilteredResult(npjt, baseQuery, searchRequest, params);

    }


    public PagedResponse<Map<String, Object>> getSiteList(String pltCode, SearchRequest searchRequest){

        String baseQuery = """
                    SELECT SM.Code,SM.Name FROM SiteMast SM WHERE SM.pltCode= :pltCode
                """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("pltCode", pltCode);

        return DmPaging.getFilteredResult(npjt, baseQuery, searchRequest, params);
    }


    public int getCounterCount(String nsGroup) {
        String sql = "SELECT COUNT(*) FROM numberSystemCounter WHERE nsGroup = :nsGroup";
        Map<String, Object> params = Map.of("nsGroup", nsGroup);
        Integer count = npjt.queryForObject(sql, params, Integer.class);
        return count != null ? count : 0;
    }


    public void deleteFromNumberSystemFormat(String nsGroup) {
        String sql = "DELETE FROM numbersystemformat WHERE nsGroup = :nsGroup";
        Map<String, Object> params = Map.of("nsGroup", nsGroup);
        npjt.update(sql, params);
    }

    public void deleteFromNumberSystem(String nsGroup) {
        String sql = "DELETE FROM numberSystem WHERE nsGroup = :nsGroup";
        Map<String, Object> params = Map.of("nsGroup", nsGroup);
        npjt.update(sql, params);
    }



    public String saveNumberSystem(NumberSystemDTO dto) {
        String nsGroup = dto.getNsGroup();
        String uName = dto.getUName();

        // Build includedVType string
        StringBuilder includedVTypeBuilder = new StringBuilder(nsGroup);
        for (IncludVocherTypeDTO vType : dto.getGrdVType()) {
            if (vType.getCode() != null && !vType.getCode().isBlank()) {
                includedVTypeBuilder.append(",").append(vType.getCode());
            }
        }
        String includedVType = includedVTypeBuilder.toString();

        // Delete old entries
        Map<String, Object> params = Map.of("nsGroup", nsGroup);
        npjt.update("DELETE FROM numbersystemformat WHERE nsGroup = :nsGroup", params);
        npjt.update("DELETE FROM numberSystem WHERE nsGroup = :nsGroup", params);

        // Insert new number formats
        for (NumberFormatDTO nf : dto.getGrdNumberFormat()) {
            if (nf.getNumberFormat() != null && !nf.getNumberFormat().isBlank()) {
                Map<String, Object> nfParams = new HashMap<>();
                nfParams.put("nsGroup", nsGroup);
                nfParams.put("site_Code", nf.getSiteCode());
                nfParams.put("nsFormat", nf.getNumberFormat());
                nfParams.put("counterFormat", nf.getCounterFormat());
                nfParams.put("isYearWise", nf.getIsYearWiseCode() != null ? nf.getIsYearWiseCode() : "0");
                nfParams.put("isSiteWise", nf.getIsSiteWiseCode() != null ? nf.getIsSiteWiseCode() : "0");

                npjt.update("""
                        INSERT INTO numbersystemformat
                        (nsGroup, site_Code, nsFormat, counterFormat, isYearWise, isSiteWise)
                        VALUES (:nsGroup, :site_Code, :nsFormat, :counterFormat, :isYearWise, :isSiteWise)
                        """, nfParams);
            }
        }

        // Insert into numberSystem for base v_type
        Map<String, Object> mainParams = new HashMap<>();
        mainParams.put("nsGroup", nsGroup);
        mainParams.put("v_type", nsGroup); // Assuming primary vType = nsGroup
        mainParams.put("u_Name", uName);
        mainParams.put("includeV_Type", includedVType);
        npjt.update("""
                INSERT INTO numberSystem
                (nsGroup, v_type, u_Name, u_EntDt, includeV_Type)
                VALUES (:nsGroup, :v_type, :u_Name, GETDATE(), :includeV_Type)
                """, mainParams);

        // Insert for each additional vType
        for (IncludVocherTypeDTO vType : dto.getGrdVType()) {
            if (vType.getCode() != null && !vType.getCode().isBlank()) {
                Map<String, Object> vTypeParams = new HashMap<>();
                vTypeParams.put("nsGroup", nsGroup);
                vTypeParams.put("v_type", vType.getCode());
                vTypeParams.put("u_Name", uName);
                vTypeParams.put("includeV_Type", includedVType);

                npjt.update("""
                        INSERT INTO numberSystem
                        (nsGroup, v_type, u_Name, u_EntDt, includeV_Type)
                        VALUES (:nsGroup, :v_type, :u_Name, GETDATE(), :includeV_Type)
                        """, vTypeParams);
            }
        }

        return nsGroup;
    }



    public int executeDeleteCounter(GrdCounterDTO linkedUserGrid) {
        String sql = """
            DELETE FROM numberSystemCounter
            WHERE nsGroup = :nsGroup
              AND nsFormatValue = :nsFormatValue
              AND v_prefix = :v_prefix
              AND ISNULL(site_Code, '') = :site_Code
        """;

        MapSqlParameterSource params = new MapSqlParameterSource();

        params.addValue("nsGroup", DmUtil.xNull(linkedUserGrid.getCode()));
        params.addValue("nsFormatValue", DmUtil.xNull(linkedUserGrid.getNumberFormat()));
        params.addValue("v_prefix", DmUtil.xNull(linkedUserGrid.getVoucherPre()));
        params.addValue("site_Code", DmUtil.xNull(linkedUserGrid.getSiteCode()));

        return npjt.update(sql, params);
    }


    public String chkNumberFormat(String numberFormat, List<KeyNameDTO> numberFormatLst) {

        String tempString;
        String[] strArray;
        String result = null;

        try {
            numberFormat = DmUtil.xNull(numberFormat);
            tempString = numberFormat.toLowerCase().replaceAll("\\{", " \\{");
            tempString = numberFormat.replaceAll("\\}", "\\} ");

            for (int i = 0; i < numberFormatLst.size(); i++) {
                strArray = tempString.split("\\{" + DmUtil.xNull(numberFormatLst.get(i).getName()).toLowerCase() + "\\}");
                numberFormat = numberFormat.replaceAll("(?i)\\{" + DmUtil.xNull(numberFormatLst.get(i).getName()) + "\\}", "\\{" + DmUtil.xNull(numberFormatLst.get(i).getName()).toLowerCase() + "\\}");
                if (strArray.length > 2) {
                    throw new Exception(DmUtil.xNull(numberFormatLst.get(i).getCode()) + " Number Format is repeating, You can use only one Number Format .");
                }
            }
            result = numberFormat;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return result;
    }


    public int fetchCounterExistence(GrdCounterDTO model) {
        String sql = """
        SELECT COUNT(*) FROM numberSystemCounter
        WHERE nsFormatValue = :nsFormatValue
          AND v_prefix = :v_prefix
          AND ISNULL(site_Code, '') = :site_Code
          AND nsGroup = :nsGroup
    """;

        Map<String, Object> params = Map.of(
                "nsFormatValue", DmUtil.xNull(model.getNumberFormat()),
                "v_prefix", DmUtil.xNull(model.getVoucherPre()),
                "site_Code", DmUtil.xNull(model.getSiteCode()),
                "nsGroup", DmUtil.xNull(model.getCode())
        );

        Integer count = npjt.queryForObject(sql, params, Integer.class);
        return count != null ? count : 0;

    }


    public void updateCounter(GrdCounterDTO model) {
        String sql = """
        UPDATE numberSystemCounter SET counter = :counter
        WHERE nsFormatValue = :nsFormatValue
          AND v_prefix = :v_prefix
          AND ISNULL(site_Code, '') = :site_Code
          AND nsGroup = :nsGroup
    """;

        Map<String, Object> params = Map.of(
                "nsFormatValue", DmUtil.xNull(model.getNumberFormat()),
                "v_prefix", DmUtil.xNull(model.getVoucherPre()),
                "site_Code", DmUtil.xNull(model.getSiteCode()),
                "nsGroup", DmUtil.xNull(model.getCode()),
                "counter", DmUtil.xNull(model.getCounterFormat())
        );

        npjt.update(sql, params);
    }

    public List<GrdCounterDTO> fetchCounterListByGroup(String nsGroup) {
        String sql = """
            SELECT nsGroup, nsFormatValue, v_prefix, site_code, counter
            FROM numberSystemCounter
            WHERE nsGroup = :nsGroup
        """;

        Map<String, Object> params = Map.of("nsGroup", DmUtil.xNull(nsGroup));

        return npjt.query(sql, params, (rs, rowNum) -> {
            GrdCounterDTO item = new GrdCounterDTO();
            item.setCode(DmUtil.xNull(rs.getString("nsGroup")));
            item.setNumberFormat(DmUtil.xNull(rs.getString("nsFormatValue")));
            item.setVoucherPre(DmUtil.xNull(rs.getString("v_prefix")));
            item.setSiteCode(DmUtil.xNull(rs.getString("site_code")));
            item.setCounterFormat(DmUtil.xNull(rs.getString("counter")));
            return item;
        });
    }

    public void deleteCountersByGroup(String nsGroup) {
        String sql = "DELETE FROM numberSystemCounter WHERE nsGroup = :nsGroup";
        Map<String, Object> params = Map.of("nsGroup", DmUtil.xNull(nsGroup));
        npjt.update(sql, params);
    }

    public void insertCounters(List<GrdCounterDTO> counters) {
        String sql = """
        INSERT INTO numberSystemCounter
        (nsGroup, nsFormatValue, v_prefix, site_Code, counter)
        VALUES (:nsGroup, :nsFormatValue, :v_prefix, :site_Code, :counter)
    """;

        List<Map<String, Object>> batchParams = new ArrayList<>();
        for (GrdCounterDTO mdl : counters) {
            Map<String, Object> map = new HashMap<>();
            map.put("nsGroup", DmUtil.foreignKey(mdl.getCode()));
            map.put("nsFormatValue", DmUtil.xNull(mdl.getNumberFormat()));
            map.put("v_prefix", DmUtil.xNull(mdl.getVoucherPre()));
            map.put("site_Code", DmUtil.foreignKey(mdl.getSiteCode()));
            map.put("counter", DmUtil.vNull(mdl.getCounterFormat(), "0"));
            batchParams.add(map);
        }

        npjt.batchUpdate(sql, batchParams.toArray(new Map[0]));
    }


    public byte[] exportDataToCSV() {

//        KeyNameDTO keyField = new KeyNameDTO();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream, StandardCharsets.UTF_8));

        final String FILE_HEADER = "nsGroup,nsFormat,counterFormat,isYearWise,isSiteWise,includeV_Type";
        final String COMMA_DELIMITER = ",";
        final String NEW_LINE_SEPARATOR = "\n";

        byte[] result = null;

        try {

            writer.write(FILE_HEADER);
            writer.write(NEW_LINE_SEPARATOR);

            String sql = """
            SELECT
                nsf.counterFormat,
                nsf.nsFormat,
                ns.nsGroup AS nsgVType,
                ns.v_Type AS nsVType,
                vtns.short_Name AS nsGroup,
                vtin.short_Name AS includeV_Type,
                vtin.short_Name AS v_Type,
                nsf.isSiteWise,
                nsf.isYearWise
            FROM numbersystemformat nsf
            LEFT JOIN numberSystem ns ON ns.nsGroup = nsf.nsGroup
            LEFT JOIN sitemast sm ON nsf.site_Code = sm.code
            LEFT JOIN voucher_type vtin ON ns.v_Type = vtin.v_Type
            LEFT JOIN voucher_type vtns ON ns.nsGroup = vtns.v_Type
            WHERE nsf.site_Code IS NULL AND ns.v_Type IS NOT NULL
            ORDER BY ns.nsGroup
        """;

            List<Map<String, Object>> rows = npjt.queryForList(sql, Collections.emptyMap());

            String prvVType = "", lineData = "", multiIncludeType = "";
            for (Map<String, Object> row : rows) {
                String nsGroup = DmUtil.xNull(row.get("nsGroup"));
                String nsFormat = DmUtil.xNull(row.get("nsFormat"));
                String counterFormat = DmUtil.xNull(row.get("counterFormat"));
                String isYearWise = DmUtil.xNull(row.get("isYearWise"));
                String isSiteWise = DmUtil.xNull(row.get("isSiteWise"));
                String vType = DmUtil.xNull(row.get("v_Type"));

                if (!prvVType.equalsIgnoreCase(nsGroup)) {
                    if (!lineData.isEmpty()) {
                        writer.write(lineData + COMMA_DELIMITER + "\"" + multiIncludeType + "\"" + NEW_LINE_SEPARATOR);
                    }
                    lineData = "\"" + nsGroup + "\"" + COMMA_DELIMITER;
                    lineData += "\"" + nsFormat + "\"" + COMMA_DELIMITER;
                    lineData += "\"" + counterFormat + "\"" + COMMA_DELIMITER;
                    lineData += "\"" + isYearWise + "\"" + COMMA_DELIMITER;
                    lineData += "\"" + isSiteWise + "\"";
                    multiIncludeType = vType;
                } else {
                    if (!multiIncludeType.isEmpty()) {
                        multiIncludeType += ",";
                    }
                    multiIncludeType += vType;
                }
                prvVType = nsGroup;
            }

            if (!lineData.isEmpty()) {
                writer.write(lineData + COMMA_DELIMITER + "\"" + multiIncludeType + "\"" + NEW_LINE_SEPARATOR);
            }

            writer.flush();


            result = outStream.toByteArray(); // <-- Your byte[] here


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
                outStream.close();
            } catch (Exception ignored) {
            }
        }

        return result;
    }


    public File exportDataToCSVFile() {
        String FILE_HEADER = "nsGroup,nsFormat,counterFormat,isYearWise,isSiteWise,includeV_Type";
        String COMMA_DELIMITER = ",";
        String NEW_LINE_SEPARATOR = "\n";

        //File csvFile = null;
        BufferedWriter writer = null;

        try {

            String dirPath = "C:/downloads/exports/";
            String fileName = "number_system_" + System.currentTimeMillis() + ".csv";
            File csvFile = new File(dirPath + fileName);
            // Create temp file or define a custom path
            //csvFile = File.createTempFile("number_system_", ".csv");
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8));

            writer.write(FILE_HEADER);
            writer.write(NEW_LINE_SEPARATOR);

            String sql = """
                SELECT 
                    nsf.counterFormat,
                    nsf.nsFormat,
                    ns.nsGroup AS nsgVType,
                    ns.v_Type AS nsVType,
                    vtns.short_Name AS nsGroup,
                    vtin.short_Name AS includeV_Type,
                    vtin.short_Name AS v_Type,
                    nsf.isSiteWise,
                    nsf.isYearWise
                FROM numbersystemformat nsf
                LEFT JOIN numberSystem ns ON ns.nsGroup = nsf.nsGroup
                LEFT JOIN sitemast sm ON nsf.site_Code = sm.code
                LEFT JOIN voucher_type vtin ON ns.v_Type = vtin.v_Type
                LEFT JOIN voucher_type vtns ON ns.nsGroup = vtns.v_Type
                WHERE nsf.site_Code IS NULL AND ns.v_Type IS NOT NULL
                ORDER BY ns.nsGroup
            """;

            List<Map<String, Object>> rows = npjt.queryForList(sql, Collections.emptyMap());

            String prvVType = "", lineData = "", multiIncludeType = "";
            for (Map<String, Object> row : rows) {
                String nsGroup = DmUtil.xNull(row.get("nsGroup"));
                String nsFormat = DmUtil.xNull(row.get("nsFormat"));
                String counterFormat = DmUtil.xNull(row.get("counterFormat"));
                String isYearWise = DmUtil.xNull(row.get("isYearWise"));
                String isSiteWise = DmUtil.xNull(row.get("isSiteWise"));
                String vType = DmUtil.xNull(row.get("v_Type"));

                if (!prvVType.equalsIgnoreCase(nsGroup)) {
                    if (!lineData.isEmpty()) {
                        writer.write(lineData + COMMA_DELIMITER + "\"" + multiIncludeType + "\"" + NEW_LINE_SEPARATOR);
                    }
                    lineData = "\"" + nsGroup + "\"" + COMMA_DELIMITER;
                    lineData += "\"" + nsFormat + "\"" + COMMA_DELIMITER;
                    lineData += "\"" + counterFormat + "\"" + COMMA_DELIMITER;
                    lineData += "\"" + isYearWise + "\"" + COMMA_DELIMITER;
                    lineData += "\"" + isSiteWise + "\"";
                    multiIncludeType = vType;
                } else {
                    if (!multiIncludeType.isEmpty()) {
                        multiIncludeType += ",";
                    }
                    multiIncludeType += vType;
                }
                prvVType = nsGroup;
            }

            if (!lineData.isEmpty()) {
                writer.write(lineData + COMMA_DELIMITER + "\"" + multiIncludeType + "\"" + NEW_LINE_SEPARATOR);
            }

            writer.flush();
            return csvFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException ignored) {
            }
        }




    }

    public File exportDataToCSVFiles() {
        String FILE_HEADER = "nsGroup,nsFormat,counterFormat,isYearWise,isSiteWise,includeV_Type";
        String COMMA_DELIMITER = ",";
        String NEW_LINE_SEPARATOR = "\n";

        BufferedWriter writer = null;

        try {
            String dirPath = "C:/downloads/exports/";
            new File(dirPath).mkdirs(); // Create directory if it doesn't exist
            String fileName = "number_system_" + System.currentTimeMillis() + ".csv";
            File csvFile = new File(dirPath + fileName);

            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8));
            writer.write(FILE_HEADER);
            writer.write(NEW_LINE_SEPARATOR);

            String sql = """
            SELECT 
                nsf.counterFormat,
                nsf.nsFormat,
                ns.nsGroup AS nsgVType,
                ns.v_Type AS nsVType,
                vtns.short_Name AS nsGroup,
                vtin.short_Name AS includeV_Type,
                vtin.short_Name AS v_Type,
                nsf.isSiteWise,
                nsf.isYearWise
            FROM numbersystemformat nsf
            LEFT JOIN numberSystem ns ON ns.nsGroup = nsf.nsGroup
            LEFT JOIN sitemast sm ON nsf.site_Code = sm.code
            LEFT JOIN voucher_type vtin ON ns.v_Type = vtin.v_Type
            LEFT JOIN voucher_type vtns ON ns.nsGroup = vtns.v_Type
            WHERE nsf.site_Code IS NULL AND ns.v_Type IS NOT NULL
            ORDER BY ns.nsGroup
        """;

            List<Map<String, Object>> rows = npjt.queryForList(sql, Collections.emptyMap());

            String prvVType = "", lineData = "", multiIncludeType = "";
            for (Map<String, Object> row : rows) {
                String nsGroup = DmUtil.xNull(row.get("nsGroup"));
                String nsFormat = DmUtil.xNull(row.get("nsFormat"));
                String counterFormat = DmUtil.xNull(row.get("counterFormat"));
                String isYearWise = DmUtil.xNull(row.get("isYearWise"));
                String isSiteWise = DmUtil.xNull(row.get("isSiteWise"));
                String vType = DmUtil.xNull(row.get("v_Type"));

                if (!prvVType.equalsIgnoreCase(nsGroup)) {
                    if (!lineData.isEmpty()) {
                        writer.write(lineData + COMMA_DELIMITER + "\"" + multiIncludeType + "\"" + NEW_LINE_SEPARATOR);
                    }
                    lineData = "\"" + nsGroup + "\"" + COMMA_DELIMITER +
                            "\"" + nsFormat + "\"" + COMMA_DELIMITER +
                            "\"" + counterFormat + "\"" + COMMA_DELIMITER +
                            "\"" + isYearWise + "\"" + COMMA_DELIMITER +
                            "\"" + isSiteWise + "\"";
                    multiIncludeType = vType;
                } else {
                    if (!multiIncludeType.isEmpty()) {
                        multiIncludeType += ",";
                    }
                    multiIncludeType += vType;
                }
                prvVType = nsGroup;
            }

            if (!lineData.isEmpty()) {
                writer.write(lineData + COMMA_DELIMITER + "\"" + multiIncludeType + "\"" + NEW_LINE_SEPARATOR);
            }

            writer.flush();
            return csvFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException ignored) {
            }
        }
    }













}
