package com.kakarote.crm9.erp.crm.service;

import com.kakarote.crm9.utils.R;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import java.math.BigDecimal;
import java.util.*;

public class InstrumentService {
    /**
     * 销售简报
     */
    public R queryBulletin(Integer type, String userIds) {
        //1.今天 2.昨天 3.本周 4.上周 5.本月6.上月7.本季度8.上季度9.本年10上年
        if (type == 1) {
            return R.ok().put("data", Db.findFirst(Db.getSqlPara("crm.Instrument.intraday", Kv.by("userIds", userIds))));
        } else if (type == 2) {
            return R.ok().put("data", Db.findFirst(Db.getSqlPara("crm.Instrument.yesterday", Kv.by("userIds", userIds))));
        } else if (type == 3) {
            return R.ok().put("data", Db.findFirst(Db.getSqlPara("crm.Instrument.thisWeek", Kv.by("userIds", userIds))));
        } else if (type == 4) {
            return R.ok().put("data", Db.findFirst(Db.getSqlPara("crm.Instrument.lastWeek", Kv.by("userIds", userIds))));
        } else if (type == 5) {
            return R.ok().put("data", Db.findFirst(Db.getSqlPara("crm.Instrument.theSameMonth", Kv.by("userIds", userIds))));
        } else if (type == 6) {
            return R.ok().put("data", Db.findFirst(Db.getSqlPara("crm.Instrument.lastMonth", Kv.by("userIds", userIds))));
        } else if (type == 7) {
            return R.ok().put("data", Db.findFirst(Db.getSqlPara("crm.Instrument.currentSeason", Kv.by("userIds", userIds))));
        } else if (type == 8) {
            return R.ok().put("data", Db.findFirst(Db.getSqlPara("crm.Instrument.precedingQuarter", Kv.by("userIds", userIds))));
        } else if (type == 9) {
            return R.ok().put("data", Db.findFirst(Db.getSqlPara("crm.Instrument.thisYear", Kv.by("userIds", userIds))));
        } else if (type == 10) {
            return R.ok().put("data", Db.findFirst(Db.getSqlPara("crm.Instrument.lastYear", Kv.by("userIds", userIds))));
        }
        return R.error();
    }

    /**
     * 业绩指标(按照月份查询)
     */
    public R sellMonth(String year, String userIds) {
        String[] months = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        Map<String, Object> map = new HashMap<>();
        Record record = null;
        BigDecimal contractMoneySum = new BigDecimal(0);
        BigDecimal receivablesMoneySum = new BigDecimal(0);
        String timeYearMonth = "";
        int i = 1;
        for (String month : months) {
            timeYearMonth = year + month;
            record = Db.findFirst(Db.getSqlPara("crm.Instrument.sellMonth", Kv.by("timeYearMonth", timeYearMonth).set("userIds", userIds)));
            if (record != null) {
                map.put(i++ + "月", record);
                contractMoneySum = contractMoneySum.add(record.getBigDecimal("contractMoneys"));
                receivablesMoneySum = receivablesMoneySum.add(record.getBigDecimal("receivablesMoneys"));
            }

        }
        map.put("contractMoneySum", contractMoneySum);
        map.put("receivablesMoneySum", receivablesMoneySum);
        return R.ok().put("data", map);
    }

    /**
     * 业绩指标(按照季度查询)
     */
    public R sellYears(String year, String userIds) {
        String[] quarters = {"01", "02", "03", "04"};
        Map<String, Object> map = new HashMap<>();
        String[] num = {"一", "二", "三", "四"};
        Record record = null;
        BigDecimal contractMoneySum = new BigDecimal(0);
        BigDecimal receivablesMoneySum = new BigDecimal(0);
        int i = 0;
        for (String quarter : quarters) {
            record = Db.findFirst(Db.getSqlPara("crm.Instrument.sellYears", Kv.by("year", year).set("quarter", quarter).set("userIds", userIds)));
            if (record == null) {
                continue;
            }
            contractMoneySum = contractMoneySum.add(record.getBigDecimal("contractMoneys"));
            receivablesMoneySum = receivablesMoneySum.add(record.getBigDecimal("receivablesMoneys"));
            map.put(num[i++] + "季度", record);
        }
        map.put("contractMoneySum", contractMoneySum);
        map.put("receivablesMoneySum", receivablesMoneySum);
        return R.ok().put("data", map);
    }

    /**
     * 业绩指标
     */
    public R queryPerformance(String startTime, String endTime, String userIds, Integer type, Integer status) {
        //type 1 回款 2.合同
        Record record = Db.findFirst(Db.getSqlPara("crm.Instrument.queryMoneys", Kv.by("startTime", startTime).set("endTime", endTime).set("userIds", userIds)));
        if (record == null) {
            return R.ok().put("data", new Record().set("contractMoneys", 0).set("receivablesMoneys", 0).set("achievementMoneys", 0).set("proportion", 0));
        }
        List<Integer> list = getYear(startTime, endTime);
        BigDecimal money = new BigDecimal(0);
        if (list.size() == 1) {
            Record start = Db.findFirst(Db.getSqlPara("crm.Instrument.queryTargets", Kv.by("year", list.get(0)).set("status", status).set("type", type).set("userIds", userIds)));
            Integer sta = Integer.valueOf(startTime.substring(5, 6));
            Integer en = Integer.valueOf(endTime.substring(5, 6));
            if (start != null) {
                if (sta <= 1 && en >= 1) {
                    money = money.add(new BigDecimal(start.getStr("january")));
                }
                if (sta <= 2 && en >= 1) {
                    money = money.add(new BigDecimal(start.getStr("february")));
                }
                if (sta <= 3 && en >= 1) {
                    money = money.add(new BigDecimal(start.getStr("march")));
                }
                if (sta <= 4 && en >= 1) {
                    money = money.add(new BigDecimal(start.getStr("april")));
                }
                if (sta <= 5 && en >= 1) {
                    money = money.add(new BigDecimal(start.getStr("may")));
                }
                if (sta <= 6 && en >= 1) {
                    money = money.add(new BigDecimal(start.getStr("june")));
                }
                if (sta <= 7 && en >= 1) {
                    money = money.add(new BigDecimal(start.getStr("july")));
                }
                if (sta <= 8 && en >= 1) {
                    money = money.add(new BigDecimal(start.getStr("august")));
                }
                if (sta <= 9 && en >= 1) {
                    money = money.add(new BigDecimal(start.getStr("september")));
                }
                if (sta <= 10 && en >= 1) {
                    money = money.add(new BigDecimal(start.getStr("october")));
                }
                if (sta <= 11 && en >= 1) {
                    money = money.add(new BigDecimal(start.getStr("november")));
                }
                if (sta <= 12 && en >= 1) {
                    money = money.add(new BigDecimal(start.getStr("december")));
                }
            }
        } else {
            for (int i = 1; i < list.size() - 1; i++) {
                Record r = Db.findFirst(Db.getSqlPara("crm.Instrument.queryTarget", Kv.by("year", list.get(i)).set("status", status).set("type", type).set("userIds", userIds)));
                money = money.add(new BigDecimal(r.getStr("achievementTarget")));
            }
            Record start = Db.findFirst(Db.getSqlPara("crm.Instrument.queryTargets", Kv.by("year", list.get(0)).set("status", status).set("type", type).set("userIds", userIds)));
            Integer sta = Integer.valueOf(startTime.substring(5, 6));
            if (start != null) {
                if (sta <= 1) {
                    money = money.add(new BigDecimal(start.getStr("january")));
                }
                if (sta <= 2) {
                    money = money.add(new BigDecimal(start.getStr("february")));
                }
                if (sta <= 3) {
                    money = money.add(new BigDecimal(start.getStr("march")));
                }
                if (sta <= 4) {
                    money = money.add(new BigDecimal(start.getStr("april")));
                }
                if (sta <= 5) {
                    money = money.add(new BigDecimal(start.getStr("may")));
                }
                if (sta <= 6) {
                    money = money.add(new BigDecimal(start.getStr("june")));
                }
                if (sta <= 7) {
                    money = money.add(new BigDecimal(start.getStr("july")));
                }
                if (sta <= 8) {
                    money = money.add(new BigDecimal(start.getStr("august")));
                }
                if (sta <= 9) {
                    money = money.add(new BigDecimal(start.getStr("september")));
                }
                if (sta <= 10) {
                    money = money.add(new BigDecimal(start.getStr("october")));
                }
                if (sta <= 11) {
                    money = money.add(new BigDecimal(start.getStr("november")));
                }
                if (sta <= 12) {
                    money = money.add(new BigDecimal(start.getStr("december")));
                }
            }
            Record end = Db.findFirst(Db.getSqlPara("crm.Instrument.queryTargets", Kv.by("year", list.get(list.size() - 1)).set("status", status).set("type", type).set("userIds", userIds)));
            Integer en = Integer.valueOf(endTime.substring(5, 6));
            if (end != null) {
                if (en >= 1) {
                    money = money.add(new BigDecimal(end.getStr("january")));
                }
                if (en >= 2) {
                    money = money.add(new BigDecimal(end.getStr("february")));
                }
                if (en >= 3) {
                    money = money.add(new BigDecimal(end.getStr("march")));
                }
                if (en >= 4) {
                    money = money.add(new BigDecimal(end.getStr("april")));
                }
                if (en >= 5) {
                    money = money.add(new BigDecimal(end.getStr("may")));
                }
                if (en >= 6) {
                    money = money.add(new BigDecimal(end.getStr("june")));
                }
                if (en >= 7) {
                    money = money.add(new BigDecimal(end.getStr("july")));
                }
                if (en >= 8) {
                    money = money.add(new BigDecimal(end.getStr("august")));
                }
                if (en >= 9) {
                    money = money.add(new BigDecimal(end.getStr("september")));
                }
                if (en >= 10) {
                    money = money.add(new BigDecimal(end.getStr("october")));
                }
                if (en >= 11) {
                    money = money.add(new BigDecimal(end.getStr("november")));
                }
                if (en >= 12) {
                    money = money.add(new BigDecimal(end.getStr("december")));
                }
            }
        }
        record.set("achievementMoneys", money);
        if (money.compareTo(new BigDecimal(0)) == 0) {
            record.set("proportion", 0);
        } else {
            if (type == 1) {
                record.set("proportion", new BigDecimal(record.getStr("receivablesMoneys")).multiply(new BigDecimal(100)).divide(money, 2, BigDecimal.ROUND_HALF_UP));
            } else if (type == 2) {
                record.set("proportion", new BigDecimal(record.getStr("contractMoneys")).multiply(new BigDecimal(100)).divide(money, 2, BigDecimal.ROUND_HALF_UP));
            }
        }
        return R.ok().put("data", record);
    }

    /**
     * 获取传过来的年份
     */
    private List<Integer> getYear(String startTime, String endTime) {
        List<Integer> list = new ArrayList<>();
        Integer start = Integer.valueOf(startTime.substring(0, 4));
        Integer end = Integer.valueOf(endTime.substring(0, 4));
        for (int i = start; i <= end; i++) {
            list.add(i);
        }
        return list;
    }

    /**
     * 销售漏斗
     */
    public R queryBusiness(String userIds, String deptIds, Integer typeId, Date startTime, Date endTime) {
        Record record = Db.findFirst(Db.getSqlPara("crm.Instrument.queryBusiness",
                Kv.by("userIds", userIds).set("typeId", typeId)
                        .set("startTime", startTime).set("endTime", endTime).set("deptIds", deptIds)));
        List<Record> records = Db.find(Db.getSqlPara("crm.Instrument.queryBusinessStatistics",
                Kv.by("userIds", userIds).set("typeId", typeId)
                        .set("startTime", startTime).set("endTime", endTime).set("deptIds", deptIds)));
        if(record!=null){
            record.set("record", records);
        }
        return R.ok().put("data", record);
    }
}
