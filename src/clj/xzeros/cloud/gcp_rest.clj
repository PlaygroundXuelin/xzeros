(ns xzeros.cloud.gcp-rest)

{:org/id "test-org-1"
 :org/name "test-org-1"
 }

{:rest/id "gcpListProjects"
 :rest/url "https://cloudresourcemanager.googleapis.com/v1/projects?pageToken={pageToken}"
 :rest/method :httpMethod/get
 :rest/contentType ""
 :rest/content ""
 :rest/params
 [
  ]
 :rest/storeKey "gcpListProjects"
 :rest/groupId "gcp"
 }

{:rest/id "gcpGetProjectIamPolicy"
 :rest/url "https://cloudresourcemanager.googleapis.com/v1/projects/{projectId}:getIamPolicy"
 :rest/method :httpMethod/post
 :rest/contentType ""
 :rest/content ""
 :rest/params
 [{:restParam/name "projectId"
   :restParam/key "gcpListProjects"
   :restParam/expression "?? /projects/projectId"}
  ]
 :rest/storeKey "gcpGetProjectIamPolicy"
 :rest/groupId "gcp"
 }

{:rest/id "gcpListComputeZones"
 :rest/url "https://www.googleapis.com/compute/v1/projects/{projectId}/zones"
 :rest/method :httpMethod/get
 :rest/contentType ""
 :rest/content ""
 :rest/params
 [{:restParam/name "projectId"
   :restParam/key "gcpListProjects"
   :restParam/expression "?? /projects/projectId"}
  ]
 :rest/storeKey "gcpListComputeZones"
 :rest/groupId "gcp"
 }

{:rest/id "gcpListComputeInstances"
 :rest/url "https://www.googleapis.com/compute/v1/projects/{projectId}/zones/{zone}/instances"
 :rest/method :httpMethod/get
 :rest/contentType ""
 :rest/content ""
 :rest/params
 [{:restParam/name "projectId"
   :restParam/key "gcpListProjects"
   :restParam/expression "?? /projects/projectId"}
  {:restParam/name "zone"
   :restParam/key "gcpListComputeZones"
   :restParam/expression "?? /items/*/name"}
  ]
 :rest/storeKey "gcpListComputeInstances"
 :rest/groupId "gcp"
 }
