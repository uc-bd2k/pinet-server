install.packages("devtools")
library(devtools)

R.version.string
if (!requireNamespace("BiocManager", quietly = TRUE))
  install.packages("BiocManager")

#BiocManager::install("artMS")
library(artMS)
browseVignettes("artMS")

