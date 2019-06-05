package com.dfire.platform.alchemy.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import com.dfire.platform.alchemy.domain.enumeration.JobType;
import com.dfire.platform.alchemy.domain.enumeration.JobStatus;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.InstantFilter;

/**
 * Criteria class for the {@link com.dfire.platform.alchemy.domain.Job} entity. This class is used
 * in {@link com.dfire.platform.alchemy.web.rest.JobResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /jobs?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class JobCriteria implements Serializable, Criteria {
    /**
     * Class for filtering JobType
     */
    public static class JobTypeFilter extends Filter<JobType> {

        public JobTypeFilter() {
        }

        public JobTypeFilter(JobTypeFilter filter) {
            super(filter);
        }

        @Override
        public JobTypeFilter copy() {
            return new JobTypeFilter(this);
        }

    }
    /**
     * Class for filtering JobStatus
     */
    public static class JobStatusFilter extends Filter<JobStatus> {

        public JobStatusFilter() {
        }

        public JobStatusFilter(JobStatusFilter filter) {
            super(filter);
        }

        @Override
        public JobStatusFilter copy() {
            return new JobStatusFilter(this);
        }

    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private JobTypeFilter type;

    private StringFilter remark;

    private StringFilter clusterJobId;

    private JobStatusFilter status;

    private StringFilter createdBy;

    private InstantFilter createdDate;

    private StringFilter lastModifiedBy;

    private InstantFilter lastModifiedDate;

    private LongFilter businessId;

    private LongFilter clusterId;

    private LongFilter sqlId;

    public JobCriteria(){
    }

    public JobCriteria(JobCriteria other){
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.type = other.type == null ? null : other.type.copy();
        this.remark = other.remark == null ? null : other.remark.copy();
        this.clusterJobId = other.clusterJobId == null ? null : other.clusterJobId.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.createdBy = other.createdBy == null ? null : other.createdBy.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
        this.lastModifiedBy = other.lastModifiedBy == null ? null : other.lastModifiedBy.copy();
        this.lastModifiedDate = other.lastModifiedDate == null ? null : other.lastModifiedDate.copy();
        this.businessId = other.businessId == null ? null : other.businessId.copy();
        this.clusterId = other.clusterId == null ? null : other.clusterId.copy();
        this.sqlId = other.sqlId == null ? null : other.sqlId.copy();
    }

    @Override
    public JobCriteria copy() {
        return new JobCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getName() {
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public JobTypeFilter getType() {
        return type;
    }

    public void setType(JobTypeFilter type) {
        this.type = type;
    }

    public StringFilter getRemark() {
        return remark;
    }

    public void setRemark(StringFilter remark) {
        this.remark = remark;
    }

    public StringFilter getClusterJobId() {
        return clusterJobId;
    }

    public void setClusterJobId(StringFilter clusterJobId) {
        this.clusterJobId = clusterJobId;
    }

    public JobStatusFilter getStatus() {
        return status;
    }

    public void setStatus(JobStatusFilter status) {
        this.status = status;
    }

    public StringFilter getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(StringFilter createdBy) {
        this.createdBy = createdBy;
    }

    public InstantFilter getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(InstantFilter createdDate) {
        this.createdDate = createdDate;
    }

    public StringFilter getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(StringFilter lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public InstantFilter getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(InstantFilter lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public LongFilter getBusinessId() {
        return businessId;
    }

    public void setBusinessId(LongFilter businessId) {
        this.businessId = businessId;
    }

    public LongFilter getClusterId() {
        return clusterId;
    }

    public void setClusterId(LongFilter clusterId) {
        this.clusterId = clusterId;
    }

    public LongFilter getSqlId() {
        return sqlId;
    }

    public void setSqlId(LongFilter sqlId) {
        this.sqlId = sqlId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final JobCriteria that = (JobCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(type, that.type) &&
            Objects.equals(remark, that.remark) &&
            Objects.equals(clusterJobId, that.clusterJobId) &&
            Objects.equals(status, that.status) &&
            Objects.equals(createdBy, that.createdBy) &&
            Objects.equals(createdDate, that.createdDate) &&
            Objects.equals(lastModifiedBy, that.lastModifiedBy) &&
            Objects.equals(lastModifiedDate, that.lastModifiedDate) &&
            Objects.equals(businessId, that.businessId) &&
            Objects.equals(clusterId, that.clusterId) &&
            Objects.equals(sqlId, that.sqlId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        name,
        type,
        remark,
        clusterJobId,
        status,
        createdBy,
        createdDate,
        lastModifiedBy,
        lastModifiedDate,
        businessId,
        clusterId,
        sqlId
        );
    }

    @Override
    public String toString() {
        return "JobCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (name != null ? "name=" + name + ", " : "") +
                (type != null ? "type=" + type + ", " : "") +
                (remark != null ? "remark=" + remark + ", " : "") +
                (clusterJobId != null ? "clusterJobId=" + clusterJobId + ", " : "") +
                (status != null ? "status=" + status + ", " : "") +
                (createdBy != null ? "createdBy=" + createdBy + ", " : "") +
                (createdDate != null ? "createdDate=" + createdDate + ", " : "") +
                (lastModifiedBy != null ? "lastModifiedBy=" + lastModifiedBy + ", " : "") +
                (lastModifiedDate != null ? "lastModifiedDate=" + lastModifiedDate + ", " : "") +
                (businessId != null ? "businessId=" + businessId + ", " : "") +
                (clusterId != null ? "clusterId=" + clusterId + ", " : "") +
                (sqlId != null ? "sqlId=" + sqlId + ", " : "") +
            "}";
    }

}
