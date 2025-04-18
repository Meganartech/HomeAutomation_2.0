import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import axios from "axios";
import LoggedInNavbar from "../LoggedInNavbar";
import Sidebar from "../User/Sidebar";
import Swal from "sweetalert2";

export default function Profile() {
    const [profile, setProfile] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        const fetchProfile = async () => {
            const token = localStorage.getItem("token");
            if (!token) {
                navigate("/");
                return;
            }
            try {
                const response = await axios.get("http://localhost:8081/user/profile", {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setProfile(response.data);
                console.log("Profile data get successfully");
            } catch (err) {

                Swal.fire({
                    icon: 'error',
                    title: 'Failed to fetch data',
                    text: err.message,
                    customClass: {
                        popup: 'my-swal-popup',
                        title: 'my-swal-title',
                        htmlContainer: 'my-swal-content',
                        confirmButton: 'my-swal-confirm-button',
                    }
                });
                
                setTimeout(() => navigate("/"), 2000);
                console.log("Profile Error: ", err);
            }
        };
        fetchProfile();
    }, [navigate]);

    return (
        <>
            {/* Navbar */}
            <LoggedInNavbar path={"/user/profile"} />

            {/* Content */}
            <div className="container-fluid mt-8">
                <div className="row height">
                    {/* Sidebar */}
                    <Sidebar />
                    <main className="col-md-9 col-xl-10 ms-sm-auto">
                        <div className="shadow-lg rounded p-3 my-3">

                            <h6 className="d-inline-block rounded-pill bg-123458 px-3 py-2 mb-3">Welcome, {profile?.name} 👋</h6>

                            <div className="table-responsive">
                                <div className="d-sm-table w-100">
                                    {profile && (
                                        <>
                                            <div className="d-block d-sm-table-row border">
                                                <div className="fw-bold d-block d-sm-table-cell p-2 bg-light border">Name</div>
                                                <div className="d-block d-sm-table-cell p-2 border">{profile.name}</div>
                                            </div>
                                            <div className="d-block d-sm-table-row border">
                                                <div className="fw-bold d-block d-sm-table-cell p-2 bg-light border">Email</div>
                                                <div className="d-block d-sm-table-cell p-2 border">{profile.email}</div>
                                            </div>
                                        </>
                                    )}
                                </div>
                            </div>
                        </div>

                        {/* Edit Button */}
                        <div className="text-end my-3">
                            <Link className="btn btn-123458" to='/user/profile/update'>Edit</Link>
                        </div>
                    </main>
                </div >
            </div >
        </>
    );
};


































