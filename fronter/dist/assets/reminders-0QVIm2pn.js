import{r}from"./request-6Du0e0DK.js";function t(e=24){return r.get("/reminders/upcoming",{params:{hours:e}})}function m(e){return r.post(`/reminders/${e}/read`,null,{silent:!0})}export{t as f,m};
